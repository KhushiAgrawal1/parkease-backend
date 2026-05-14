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
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BookingScheduler.class);

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
                    && b.getStartTime() != null
                    && b.getStartTime().plusMinutes(2).isBefore(LocalDateTime.now())) {

                b.setStatus(Booking.BookingStatus.CANCELLED);
                b.setExpired(true);

                // 🔥 FREE SPOT
                spotClient.freeSpot(b.getSpotId());

                repo.save(b);

                logger.info("Expired booking: {}", b.getId());
            }
        }
    }
}