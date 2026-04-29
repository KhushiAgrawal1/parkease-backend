package com.parkease.paymentservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long userId; // 🔥 NEW (IMPORTANT)

    private Double amount;

    private boolean success = true;
    private boolean refunded = false; // 🔥 already added
}