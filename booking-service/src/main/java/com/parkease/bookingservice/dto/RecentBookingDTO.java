package com.parkease.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentBookingDTO {

    private Long id;
    private String status;
    private Long spotId;
    private LocalDateTime startTime;
}