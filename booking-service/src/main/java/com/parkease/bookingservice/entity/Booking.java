package com.parkease.bookingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private Long userId;
    @NotNull
    private Long lotId;
    @NotNull
    private Long spotId;

    private String status; // BOOKED / COMPLETED

    private LocalDateTime createdAt;
}
