package com.parkease.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentBookingDTO implements Serializable {

    private Long id;
    private String status;
    private Long spotId;
    private LocalDateTime startTime;
}