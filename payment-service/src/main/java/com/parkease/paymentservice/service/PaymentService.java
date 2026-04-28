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

    public Payment processPayment(Payment payment) {

        // 🔥 Simulate payment success
        payment.setStatus("SUCCESS");

        return repo.save(payment);
    }

    public List<Payment> getAll() {
        return repo.findAll();
    }

    public Payment getByBookingId(Long bookingId) {
        return repo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
