package com.parkease.paymentservice.service;

import com.parkease.paymentservice.entity.Payment;
import com.parkease.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repo;

    // 🔥 CREATE PAYMENT
    public Payment makePayment(Payment payment) {

        List<Payment> existing = repo.findByBookingId(payment.getBookingId());

        if (!existing.isEmpty()) {
            return existing.get(existing.size() - 1); // return latest existing
        }

        payment.setSuccess(true);
        return repo.save(payment);
    }

    // 🔥 GET BY BOOKING
    public Payment getByBookingId(Long bookingId) {

        List<Payment> payments = repo.findByBookingId(bookingId);

        if (payments.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        return payments.get(payments.size() - 1); // latest payment
    }

    // 🔥 REFUND (FIXED)
    public Payment refundPayment(Long bookingId) {

        List<Payment> payments = repo.findByBookingId(bookingId);

        if (payments.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        Payment payment = payments.get(payments.size() - 1); // latest

        if (!payment.isSuccess()) {
            throw new RuntimeException("Cannot refund failed payment");
        }

        if (payment.isRefunded()) {
            throw new RuntimeException("Already refunded");
        }

        payment.setRefunded(true);

        return repo.save(payment);
    }

    // 🔥 PAYMENT HISTORY
    public List<Payment> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }
}