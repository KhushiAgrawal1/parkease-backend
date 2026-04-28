package com.parkease.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping("/notify")
    String sendNotification(Map<String, Object> request);
}