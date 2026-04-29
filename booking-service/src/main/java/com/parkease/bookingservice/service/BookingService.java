package com.parkease.bookingservice.service;
import com.parkease.bookingservice.dto.RecentBookingDTO;
import com.parkease.bookingservice.client.NotificationClient;
import com.parkease.bookingservice.client.PaymentClient;
import com.parkease.bookingservice.client.SpotClient;
import com.parkease.bookingservice.client.VehicleClient;
import com.parkease.bookingservice.dto.*;
import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.parkease.bookingservice.dto.PaymentResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private BookingRepository repo;

    @Autowired
    private SpotClient spotClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private VehicleClient vehicleClient;

    @Autowired
    private NotificationClient notificationClient;

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

        // 1. Validate vehicle
        if (booking.getVehicleId() == null) {
            throw new RuntimeException("Vehicle is required for booking");
        }

        try {
            vehicleClient.getVehicle(booking.getVehicleId());
        } catch (Exception e) {
            throw new RuntimeException("Vehicle not found");
        }

        // 2. Check spot
        ParkingSpot spot = spotClient.getSpot(booking.getSpotId());

        if ("OCCUPIED".equalsIgnoreCase(spot.getStatus())) {
            throw new RuntimeException("Spot already occupied");
        }

        // 3. Prevent double booking
        List<Booking> existingBookings =
                repo.findBySpotIdAndStatusIn(
                        booking.getSpotId(),
                        List.of(
                                Booking.BookingStatus.RESERVED,
                                Booking.BookingStatus.ACTIVE
                        )
                );

        if (!existingBookings.isEmpty()) {
            throw new RuntimeException("Spot already booked");
        }

        // 4. Set booking data
        booking.setStatus(Booking.BookingStatus.RESERVED);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStartTime(LocalDateTime.now());
        booking.setEndTime(LocalDateTime.now().plusHours(1));

        Booking saved = repo.save(booking);

        // 🔥 5. PAYMENT
        Payment payment = new Payment();
        payment.setBookingId(saved.getId());
        payment.setUserId(saved.getUserId()); // ✅ IMPORTANT
        payment.setAmount(calculateAmount(saved));

        Payment response = paymentClient.makePayment(payment);

        if (response == null || !response.isSuccess()) {
            saved.setStatus(Booking.BookingStatus.FAILED);
            repo.save(saved);
            throw new RuntimeException("Payment failed");
        }

        // 6. Occupy spot
        spotClient.occupySpot(saved.getSpotId());

        // 7. Notification
        notificationClient.sendNotification(
                Map.of(
                        "email", "agrawalkhushi267@gmail.com",
                        "message", "Booking Confirmed for Spot " + booking.getSpotId()
                )
        );

        return repo.save(saved);
    }

    // 🔥 CHECK-IN
    public Booking checkIn(Long id) {
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(Booking.BookingStatus.ACTIVE);

        return repo.save(booking);
    }

    // 🔥 CHECK-OUT
    public Booking checkOut(Long id) {

        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(Booking.BookingStatus.COMPLETED);

        // free spot
        spotClient.freeSpot(booking.getSpotId());

        double amount = calculateAmount(booking);

        // 🔥 PAYMENT (final charge)
        Payment payment = new Payment();
        payment.setBookingId(id);
        payment.setUserId(booking.getUserId()); // ✅ IMPORTANT
        payment.setAmount(amount);

        paymentClient.makePayment(payment);

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
            System.out.println("Refund failed: " + e.getMessage());
        }

        return repo.save(booking);
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

        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            throw new RuntimeException("Booking time not set properly");
        }

        long hours = Duration.between(
                booking.getStartTime(),
                booking.getEndTime()
        ).toHours();

        return Math.max(hours, 1) * 50;
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

        if (response == null || !response.isSuccess()) {
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

        return new AnalyticsResponse(
                totalBookings,
                activeBookings,
                totalRevenue
        );
    }

    public BookingDetailsResponse getBookingDetails(Long id) {

        // 1. booking
        Booking booking = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. spot
        Object spot = spotClient.getSpot(booking.getSpotId());

        // 3. vehicle
        Object vehicle = vehicleClient.getVehicle(booking.getVehicleId());

        // 4. payment

        PaymentResponse payment = paymentClient.getPaymentByBooking(id);

        return new BookingDetailsResponse(
                booking,
                spot,
                vehicle,
                payment
        );
    }

    public List<RecentBookingDTO> getRecentBookings(Long userId) {

        return repo.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(b -> new RecentBookingDTO(
                        b.getId(),
                        b.getStatus().name(),
                        b.getSpotId(),
                        b.getStartTime()
                ))
                .toList();
    }
}