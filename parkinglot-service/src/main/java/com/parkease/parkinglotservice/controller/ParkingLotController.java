package com.parkease.parkinglotservice.controller;

import com.parkease.parkinglotservice.entity.ParkingLot;
import com.parkease.parkinglotservice.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lots")
public class ParkingLotController {

    @Autowired
    private ParkingLotService service;

    @PostMapping
    public ParkingLot create(@RequestBody ParkingLot lot) {
        return service.create(lot);
    }

    @GetMapping
    public List<ParkingLot> getAll() {
        return service.getAll();
    }
}
