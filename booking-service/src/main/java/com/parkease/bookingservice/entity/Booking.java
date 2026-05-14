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
        PENDING_PAYMENT,
        RESERVED,
        ACTIVE,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    public enum BookingType {
        PRE_BOOKING,
        WALK_IN
    }

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    private Double totalAmount;

    private Long userId;
    private Long lotId;
    private Long spotId;

    @NotNull
    private Long vehicleId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;

    private LocalDateTime createdAt;

    private boolean expired;
}