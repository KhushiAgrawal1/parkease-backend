package com.parkease.analyticsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "parkinglot-service")
public interface LotClient {
    @GetMapping("/lots")
    List<Object> getAllLots();
}
