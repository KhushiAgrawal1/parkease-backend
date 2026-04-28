package com.parkease.bookingservice.service;

import com.parkease.bookingservice.client.NotificationClient;
import com.parkease.bookingservice.client.PaymentClient;
import com.parkease.bookingservice.client.SpotClient;
import com.parkease.bookingservice.dto.ParkingSpot;
import com.parkease.bookingservice.dto.Payment;
import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository repo;

    @Autowired
    private SpotClient spotClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private NotificationClient notificationClient;

    // 🔹 GET ALL BOOKINGS
    public List<Booking> getAll() {
        log.info("Fetching all bookings");
        return repo.findAll();
    }

    public Booking create(Booking booking) {

        log.info("Booking request received: {}", booking);

        // 🔥 STEP 1: Get Spot Details
        ParkingSpot spot = spotClient.getSpot(booking.getSpotId());

        if ("OCCUPIED".equalsIgnoreCase(spot.getStatus())) {
            log.error("Spot {} is already occupied", booking.getSpotId());
            throw new RuntimeException("Spot already occupied");
        }

        // 🔥 STEP 2: Save booking as PENDING
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        Booking saved = repo.save(booking);
        log.info("Booking saved with ID {}", saved.getId());

        try {
            // 🔥 STEP 3: Call Payment Service
            Payment payment = new Payment();
            payment.setBookingId(saved.getId());
            payment.setAmount(100.0);

            Payment response = paymentClient.makePayment(payment);

            if (!"SUCCESS".equalsIgnoreCase(response.getStatus())) {
                throw new RuntimeException("Payment failed");
            }

            log.info("Payment successful for booking {}", saved.getId());

            // 🔥 STEP 4: Occupy Spot
            spotClient.occupySpot(saved.getSpotId());
            log.info("Spot {} marked as OCCUPIED", saved.getSpotId());

            // 🔥 STEP 5: Update booking status
            saved.setStatus("BOOKED");
            Booking finalBooking = repo.save(saved);

            // 🔥 STEP 6: Send Notification
            notificationClient.sendNotification(
                    Map.of(
                            "email", "agrawalkhushi267@gmail.com",   // 🔥 replace with your real email
                            "message", "Booking Confirmed for Spot " + booking.getSpotId()
                    )
            );

            log.info("Booking {} confirmed", finalBooking.getId());

            return finalBooking;

        } catch (Exception e) {

            // 🔥 IMPORTANT FIX: rollback booking if anything fails
            log.error("Error occurred, cancelling booking {}", saved.getId());

            saved.setStatus("FAILED");
            repo.save(saved);

            throw new RuntimeException("Booking failed: " + e.getMessage());
        }
    }

    // 🔹 CANCEL BOOKING
    public Booking cancel(Long id) {

        log.info("Cancel request received for bookingId: {}", id);

        Booking booking = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found with id: {}", id);
                    return new RuntimeException("Booking not found");
                });

        // 🔥 FREE THE SPOT
        log.info("Freeing spot {}", booking.getSpotId());
        spotClient.freeSpot(booking.getSpotId());

        booking.setStatus("CANCELLED");
        Booking updated = repo.save(booking);

        log.info("Booking {} cancelled and spot freed", id);

        return updated;
    }

    public List<Booking> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public Booking getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<Booking> getByStatus(String status) {
        return repo.findByStatus(status);
    }
}