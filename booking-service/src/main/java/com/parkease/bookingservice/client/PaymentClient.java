package com.parkease.bookingservice.client;

import com.parkease.bookingservice.dto.Payment;
import com.parkease.bookingservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "PAYMENT-SERVICE") // ✅ FIXED (uppercase)
public interface PaymentClient {

    @PostMapping("/payments")
    Payment makePayment(@RequestBody Payment payment);

    @PutMapping("/payments/refund/{bookingId}")
    Payment refundPayment(@PathVariable Long bookingId);

    @GetMapping("/payments/booking/{bookingId}")
    PaymentResponse getPaymentByBooking(@PathVariable Long bookingId);
}