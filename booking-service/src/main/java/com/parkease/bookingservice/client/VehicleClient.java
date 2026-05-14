package com.parkease.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-service")
public interface VehicleClient {

    @GetMapping("/vehicles/{id}")
    Object getVehicle(@PathVariable("id") Long id);
}
