package com.parkease.bookingservice.scheduler;

import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.repository.BookingRepository;
import com.parkease.bookingservice.client.SpotClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingRepository repo;
    private final SpotClient spotClient;

    public BookingScheduler(BookingRepository repo, SpotClient spotClient) {
        this.repo = repo;
        this.spotClient = spotClient;
    }

    // 🔥 Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void expireBookings() {

        List<Booking> bookings = repo.findAll();

        for (Booking b : bookings) {

            if (b.getStatus() == Booking.BookingStatus.RESERVED
                    && b.getStartTime() != null) {

                // 🔥 15 min expiry buffer
                if (b.getStartTime().plusMinutes(2).isBefore(LocalDateTime.now())) {

                    b.setStatus(Booking.BookingStatus.CANCELLED);
                    b.setExpired(true);

                    // 🔥 FREE SPOT
                    spotClient.freeSpot(b.getSpotId());

                    repo.save(b);

                    System.out.println("Expired booking: " + b.getId());
                }
            }
        }
    }
}