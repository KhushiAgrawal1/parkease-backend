package com.parkease.spotservice.controller;

import com.parkease.spotservice.entity.ParkingSpot;
import com.parkease.spotservice.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spots")
public class SpotController {

    @Autowired
    private SpotService service;

    @PostMapping
    public ParkingSpot create(@RequestBody ParkingSpot spot) {
        return service.create(spot);
    }

    @GetMapping("/{lotId}")
    public List<ParkingSpot> getByLot(@PathVariable Long lotId) {
        return service.getByLot(lotId);
    }

    @PutMapping("/occupy/{id}")
    public ParkingSpot occupySpot(@PathVariable Long id) {
        return service.occupySpot(id);
    }

    @GetMapping("/single/{id}")
    public ParkingSpot getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/free/{id}")
    public ParkingSpot freeSpot(@PathVariable Long id) {
        return service.freeSpot(id);
    }

    @GetMapping("/available/{lotId}")
    public List<ParkingSpot> getAvailable(@PathVariable Long lotId) {
        return service.getAvailableSpots(lotId);
    }
}