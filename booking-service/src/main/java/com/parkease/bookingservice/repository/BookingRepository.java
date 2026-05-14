package com.parkease.bookingservice.repository;

import com.parkease.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(String status);

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByStatusAndStartTimeBefore(
            Booking.BookingStatus status,
            LocalDateTime time
    );
    List<Booking> findBySpotIdAndStatus(Long spotId, Booking.BookingStatus status);

    List<Booking> findBySpotIdAndStatusIn(Long spotId, List<Booking.BookingStatus> statuses);

    List<Booking> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
