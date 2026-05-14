package com.parkease.bookingservice.service;
import com.parkease.bookingservice.dto.RecentBookingDTO;

import com.parkease.bookingservice.client.PaymentClient;
import com.parkease.bookingservice.client.SpotClient;
import com.parkease.bookingservice.client.VehicleClient;
import com.parkease.bookingservice.client.LotClient;
import com.parkease.bookingservice.dto.*;
import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.parkease.bookingservice.dto.PaymentResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.parkease.bookingservice.config.RabbitMQConfig;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class BookingService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository repo;

    @Autowired
    private SpotClient spotClient;

    @Autowired
    private LotClient lotClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private VehicleClient vehicleClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public List<Booking> getAll() {
        return repo.findAll();
    }

    public Map<String, Long> getStatusCount() {

        List<Booking> bookings = repo.findAll();

        Map<String, Long> result = new HashMap<>();

        for (Booking.BookingStatus status : Booking.BookingStatus.values()) {

            long count = bookings.stream()
                    .filter(b -> b.getStatus() == status)
                    .count();

            result.put(status.name(), count);
        }

        return result;
    }

    // 🔥 CREATE BOOKING
    public Booking create(Booking booking) {

        // 0. Validate IDs
        if (booking.getUserId() == null || booking.getUserId() <= 0) throw new RuntimeException("Valid User ID is required");
        if (booking.getLotId() == null || booking.getLotId() <= 0) throw new RuntimeException("Valid Lot ID is required");
        if (booking.getSpotId() == null || booking.getSpotId() <= 0) throw new RuntimeException("Valid Spot ID is required");
        if (booking.getVehicleId() == null || booking.getVehicleId() <= 0) throw new RuntimeException("Valid Vehicle ID is required");

        // 1. Validate vehicle
        try {
            vehicleClient.getVehicle(booking.getVehicleId());
        } catch (Exception e) {
            throw new RuntimeException("Vehicle not found or unreachable");
        }

        // 2. Check spot and lot
        ParkingSpot spot;
        try {
            spot = spotClient.getSpot(booking.getSpotId());
        } catch (Exception e) {
            throw new RuntimeException("Spot not found or unreachable");
        }

        if ("OCCUPIED".equalsIgnoreCase(spot.getStatus())) {
            throw new RuntimeException("Spot already occupied");
        }

        // 3. Prevent double booking in booking-service
        List<Booking> existingBookings = repo.findBySpotIdAndStatusIn(
                booking.getSpotId(),
                List.of(Booking.BookingStatus.RESERVED, Booking.BookingStatus.ACTIVE)
        );

        if (!existingBookings.isEmpty()) {
            throw new RuntimeException("Spot already booked for the selected time");
        }

        // 4. Set booking data
        if (booking.getBookingType() == null) {
            booking.setBookingType(Booking.BookingType.PRE_BOOKING);
        }
        
        booking.setCreatedAt(LocalDateTime.now());
        
        if (booking.getBookingType() == Booking.BookingType.WALK_IN) {
            // Walk-in implies immediate physical presence
            booking.setStartTime(LocalDateTime.now());
            booking.setActualCheckInTime(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.ACTIVE);
        } else {
            // Pre-booking validation
            if (booking.getStartTime() == null) {
                throw new RuntimeException("Start Time is required for Pre-Booking");
            }
            if (booking.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
                throw new RuntimeException("Cannot book in the past");
            }
            booking.setStatus(Booking.BookingStatus.RESERVED);
        }

        // 5. Update spot status in spot-service
        try {
            if (booking.getBookingType() == Booking.BookingType.WALK_IN) {
                spotClient.occupySpot(booking.getSpotId());
            } else {
                // For pre-booking, we mark as RESERVED
                // Note: We need to ensure spot-service supports this or just use occupy for now if not.
                // Since spot-service only has occupy/free, we'll use occupy but it's better to add reserve.
                spotClient.occupySpot(booking.getSpotId()); 
            }
        } catch (Exception e) {
            logger.error("Failed to update spot status: {}", e.getMessage());
            throw new RuntimeException("Failed to update spot status in spot-service");
        }

        Booking saved = repo.save(booking);

        // 6. Notify User and Manager via RabbitMQ (Async/Optional)
        try {
            String htmlMessage = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px; background-color: #ffffff;\">"
                + "<h2 style=\"color: #2563eb; margin-bottom: 16px;\">New Booking Confirmed!</h2>"
                + "<p style=\"color: #334155; font-size: 16px; margin-bottom: 24px;\">Your booking for <strong>Spot #" + saved.getSpotId() + "</strong> has been created successfully.</p>"
                + "<div style=\"background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 15px; margin-bottom: 24px;\">"
                + "<div>Booking ID: <strong>#" + saved.getId() + "</strong></div>"
                + "<div>Type: <strong>" + saved.getBookingType() + "</strong></div>"
                + "</div>"
                + "<p style=\"color: #64748b; font-size: 14px;\">Thank you for choosing ParkEase.</p>"
                + "</div>";

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY, 
                Map.of(
                    "email", "agrawalkhushi267@gmail.com",
                    "message", htmlMessage,
                    "userId", saved.getUserId().toString()
                )
            );
        } catch (Exception e) {
            logger.warn("Could not send booking notification: {}", e.getMessage());
            // We don't throw exception here because booking is already saved
        }

        return saved;
    }

    // 🔥 SIMULATE PAYMENT (POST-PAID)
    public Booking pay(Long id) {
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking is not pending payment");
        }

        PaymentResponse response;
        try {
            response = paymentClient.getPaymentByBooking(booking.getId());
        } catch (Exception e) {
            throw new RuntimeException("Payment service unreachable");
        }

        if (response == null || !"PAID".equals(response.getStatus())) {
            throw new RuntimeException("Payment is not PAID in payment-service");
        }

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        Booking saved = repo.save(booking);

        // 2. Notification via RabbitMQ
        String htmlMessage = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px; background-color: #ffffff;\">"
            + "<div style=\"text-align: center; margin-bottom: 24px;\">"
            + "<h1 style=\"color: #16a34a; margin: 0; font-size: 28px;\">Payment Confirmed</h1>"
            + "<p style=\"color: #64748b; margin-top: 4px; font-size: 14px; letter-spacing: 1px;\">PARKEASE RECEIPT</p>"
            + "</div>"
            + "<p style=\"color: #334155; font-size: 16px; line-height: 1.5; margin-bottom: 24px;\">Your payment for <strong>Booking #" + saved.getId() + "</strong> has been successfully processed.</p>"
            + "<div style=\"background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 20px; text-align: center; margin-bottom: 24px;\">"
            + "<div style=\"color: #64748b; font-size: 14px; text-transform: uppercase;\">Amount Paid</div>"
            + "<div style=\"font-size: 32px; font-weight: bold; color: #0f172a;\">₹" + saved.getTotalAmount() + "</div>"
            + "</div>"
            + "<p style=\"color: #64748b; font-size: 14px; text-align: center;\">Thank you for choosing ParkEase.</p>"
            + "</div>";

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY, 
                Map.of(
                        "email", "agrawalkhushi267@gmail.com",
                        "message", htmlMessage
                )
        );

        return saved;
    }

    // 🔥 CHECK-IN
    public Booking checkIn(Long id) {
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setActualCheckInTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.ACTIVE);

        return repo.save(booking);
    }

    // 🔥 EXTEND BOOKING
    public Booking extend(Long id, int extraHours) {
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.ACTIVE && booking.getStatus() != Booking.BookingStatus.RESERVED) {
            throw new RuntimeException("Can only extend ACTIVE or RESERVED bookings");
        }

        LocalDateTime newEndTime = booking.getEndTime().plusHours(extraHours);
        booking.setEndTime(newEndTime);
        
        double newAmount = calculateAmount(booking);
        double additionalAmount = newAmount - booking.getTotalAmount();
        
        booking.setTotalAmount(newAmount);
        
        // 🔥 PAYMENT (charge for extension)
        Payment payment = new Payment();
        payment.setBookingId(id);
        payment.setUserId(booking.getUserId());
        payment.setAmount(additionalAmount);
        
        Payment response = paymentClient.makePayment(payment);
        if (response == null || response.getStatus() != Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment for extension failed");
        }
        
        // 7. Notification via RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                Map.of(
                        "email", "agrawalkhushi267@gmail.com",
                        "message", "Booking Extended by " + extraHours + " hours for Spot " + booking.getSpotId()
                )
        );

        return repo.save(booking);
    }

    // 🔥 CHECK-OUT (POST-PAID)
    public Booking checkOut(Long id) {

        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setActualCheckOutTime(LocalDateTime.now());
        
        // Calculate the actual post-paid amount based on actual time
        double amount = calculateAmount(booking);
        booking.setTotalAmount(amount);

        booking.setStatus(Booking.BookingStatus.PENDING_PAYMENT);

        // free spot
        try {
            spotClient.freeSpot(booking.getSpotId());
        } catch (Exception e) {
            logger.error("Failed to free spot", e);
        }

        return repo.save(booking);
    }

    // 🔥 CANCEL BOOKING
    public Booking cancel(Long id) {

        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        spotClient.freeSpot(booking.getSpotId());

        // 🔥 REFUND
        try {
            paymentClient.refundPayment(booking.getId());
        } catch (Exception e) {
            logger.error("Refund failed: {}", e.getMessage(), e);
        }

        return repo.save(booking);
    }

    // 🔥 MANUAL REFUND
    public Booking refund(Long id) {
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getStatus() != Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Can only refund CANCELLED bookings");
        }

        paymentClient.refundPayment(booking.getId());
        return booking;
    }

    public List<Booking> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public Booking getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<Booking> getByStatus(Booking.BookingStatus status) {
        return repo.findByStatus(status);
    }

    // 🔥 PRICE LOGIC
    public double calculateAmount(Booking booking) {

        LocalDateTime start = booking.getActualCheckInTime() != null ? booking.getActualCheckInTime() : booking.getStartTime();
        LocalDateTime end = booking.getActualCheckOutTime() != null ? booking.getActualCheckOutTime() : 
                           (booking.getEndTime() != null ? booking.getEndTime() : LocalDateTime.now());

        if (start == null) {
            return 0.0;
        }

        long hours = Duration.between(start, end).toHours();
        
        long chargeableHours = Math.max(hours, 1);
        
        double pricePerHour = 50.0;
        try {
            ParkingSpot spot = spotClient.getSpot(booking.getSpotId());
            if (spot != null && spot.getPricePerHour() != null) {
                pricePerHour = spot.getPricePerHour();
                
                try {
                    Map<String, Object> lot = lotClient.getLot(spot.getLotId());
                    if (lot != null && lot.get("surgeActive") != null && (Boolean) lot.get("surgeActive")) {
                        pricePerHour *= 1.5; // 50% Surge
                    }
                } catch (Exception e) {
                    logger.warn("Could not check lot surge pricing", e);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not fetch spot price, defaulting to 50", e);
        }

        return chargeableHours * pricePerHour;
    }

    // 🔥 RETRY PAYMENT
    public Booking retryPayment(Long id) {

        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.FAILED) {
            throw new RuntimeException("Payment retry allowed only for FAILED bookings");
        }

        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setUserId(booking.getUserId()); // ✅ IMPORTANT
        payment.setAmount(calculateAmount(booking));

        Payment response = paymentClient.makePayment(payment);

        if (response == null || response.getStatus() != Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment retry failed");
        }

        booking.setStatus(Booking.BookingStatus.RESERVED);

        spotClient.occupySpot(booking.getSpotId());

        return repo.save(booking);
    }

    public AnalyticsResponse getAnalytics() {

        List<Booking> bookings = repo.findAll();

        long totalBookings = bookings.size();

        long activeBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.ACTIVE)
                .count();

        double totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                .mapToDouble(this::calculateAmount)
                .sum();

        // Mock/Calculate most booked cities and vehicle types for demonstration
        Map<String, Long> cities = new HashMap<>();
        cities.put("Mumbai", totalBookings > 0 ? (totalBookings * 40 / 100) : 0L);
        cities.put("Delhi", totalBookings > 0 ? (totalBookings * 30 / 100) : 0L);
        cities.put("Bangalore", totalBookings > 0 ? (totalBookings * 20 / 100) : 0L);
        cities.put("Pune", totalBookings > 0 ? (totalBookings * 10 / 100) : 0L);

        Map<String, Long> vehicles = new HashMap<>();
        vehicles.put("4W", totalBookings > 0 ? (totalBookings * 60 / 100) : 0L);
        vehicles.put("2W", totalBookings > 0 ? (totalBookings * 30 / 100) : 0L);
        vehicles.put("HEAVY", totalBookings > 0 ? (totalBookings * 10 / 100) : 0L);

        return new AnalyticsResponse(
                totalBookings,
                activeBookings,
                totalRevenue,
                cities,
                vehicles
        );
    }

    public BookingDetailsResponse getBookingDetails(Long id) {

        // 1. booking
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. spot
        Object spot = null;
        try {
            spot = spotClient.getSpot(booking.getSpotId());
        } catch (Exception e) {
            logger.warn("Spot {} not found for booking {}", booking.getSpotId(), id);
        }

        // 3. vehicle
        Object vehicle = null;
        try {
            vehicle = vehicleClient.getVehicle(booking.getVehicleId());
        } catch (Exception e) {
            logger.warn("Vehicle {} not found for booking {}", booking.getVehicleId(), id);
        }

        // 4. payment

        PaymentResponse payment = null;
        try {
            payment = paymentClient.getPaymentByBooking(id);
        } catch (Exception e) {
            logger.warn("No payment found for booking {}", id);
        }

        return new BookingDetailsResponse(
                booking,
                spot,
                vehicle,
                payment
        );
    }

    // @Cacheable(value = "recentBookings", key = "#userId")

    public List<RecentBookingDTO> getRecentBookings(Long userId) {

        return repo.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(b -> new RecentBookingDTO(
                        b.getId(),
                        b.getStatus() != null ? b.getStatus().name() : "UNKNOWN",
                        b.getSpotId(),
                        b.getStartTime()
                ))
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void autoCancelExpiredPreBookings() {
        logger.info("Running auto-cancel job for expired pre-bookings...");
        List<Booking> reservedBookings = repo.findByStatus(Booking.BookingStatus.RESERVED);
        LocalDateTime now = LocalDateTime.now();
        
        for (Booking booking : reservedBookings) {
            // Give a 5-minute grace period
            if (booking.getStartTime() != null && booking.getStartTime().plusMinutes(5).isBefore(now)) {
                logger.info("Auto-cancelling expired booking: {}", booking.getId());
                cancel(booking.getId());
            }
        }
    }
}