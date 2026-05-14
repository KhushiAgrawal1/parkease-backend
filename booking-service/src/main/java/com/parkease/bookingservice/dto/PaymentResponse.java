package com.parkease.bookingservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private String status;
    private String mode;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}