package com.parkease.bookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue
    private Long id;

    public enum BookingStatus {
        RESERVED,
        ACTIVE,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Long userId;
    private Long lotId;
    private Long spotId;

    @NotNull
    private Long vehicleId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime createdAt;

    private boolean expired;
}