package com.parkease.bookingservice.client;

import com.parkease.bookingservice.dto.ParkingSpot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "spot-service")
public interface SpotClient {

    @GetMapping("/spots/single/{id}")
    ParkingSpot getSpot(@PathVariable("id") Long id);

    @PutMapping("/spots/occupy/{id}")
    void occupySpot(@PathVariable("id") Long id);

    // 🔥 ADD THIS
    @PutMapping("/spots/free/{id}")
    void freeSpot(@PathVariable("id") Long id);
}