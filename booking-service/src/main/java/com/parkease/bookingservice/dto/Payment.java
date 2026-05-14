package com.parkease.bookingservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Payment {
    public enum PaymentStatus {
        PENDING, PAID, REFUNDED, FAILED
    }

    public enum PaymentMode {
        CARD, UPI, WALLET, CASH
    }

    private Long id;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private PaymentStatus status;
    private PaymentMode mode;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}
