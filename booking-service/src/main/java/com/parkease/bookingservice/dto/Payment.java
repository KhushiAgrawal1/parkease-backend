package com.parkease.bookingservice.dto;

import lombok.Data;

@Data
public class Payment {
    private Long bookingId;
    private Double amount;
    private String status;
}
