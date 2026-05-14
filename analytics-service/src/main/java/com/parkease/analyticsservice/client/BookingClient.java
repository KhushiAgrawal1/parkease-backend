package com.parkease.analyticsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@FeignClient(name = "booking-service")
public interface BookingClient {
    @GetMapping("/bookings/analytics")
    Map<String, Object> getAnalytics();
}
