package com.parkease.bookingservice.dto;

import lombok.Data;

@Data
public class ParkingSpot {
    private Long id;
    private Long lotId;
    private String spotNumber;
    private String status;
}