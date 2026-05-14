package com.parkease.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "PARKINGLOT-SERVICE")
public interface LotClient {

    @GetMapping("/lots/{id}")
    Map<String, Object> getLot(@PathVariable("id") Long id);
}
