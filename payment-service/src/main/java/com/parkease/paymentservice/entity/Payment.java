package com.parkease.paymentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    public enum PaymentStatus {
        PENDING, PAID, REFUNDED, FAILED
    }

    public enum PaymentMode {
        CARD, UPI, WALLET, CASH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long userId; // 🔥 NEW (IMPORTANT)

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMode mode;

    private String transactionId;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}