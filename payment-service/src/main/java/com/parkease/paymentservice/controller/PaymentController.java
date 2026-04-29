package com.parkease.paymentservice.controller;

import com.parkease.paymentservice.entity.Payment;
import com.parkease.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService service;

    @PostMapping
    public Payment makePayment(@RequestBody Payment payment) {
        return service.makePayment(payment);
    }

    @GetMapping("/booking/{bookingId}")
    public Payment getByBooking(@PathVariable Long bookingId) {
        return service.getByBookingId(bookingId);
    }

    @PutMapping("/refund/{bookingId}")
    public Payment refund(@PathVariable Long bookingId) {
        return service.refundPayment(bookingId);
    }

    // 🔥 NEW: USER PAYMENT HISTORY
    @GetMapping("/user/{userId}")
    public List<Payment> getUserPayments(@PathVariable Long userId) {
        return service.getByUser(userId);
    }
}