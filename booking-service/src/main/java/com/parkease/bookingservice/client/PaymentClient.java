package com.parkease.bookingservice.client;

import com.parkease.bookingservice.dto.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments")
    Payment makePayment(@RequestBody Payment payment);
}
