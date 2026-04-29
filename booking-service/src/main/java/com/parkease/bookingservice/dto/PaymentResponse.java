package com.parkease.bookingservice.dto;

import lombok.Data;

@Data
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private boolean success;
    private boolean refunded;
}