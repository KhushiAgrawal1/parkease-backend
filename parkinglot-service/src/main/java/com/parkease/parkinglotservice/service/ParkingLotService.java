package com.parkease.parkinglotservice.service;

import com.parkease.parkinglotservice.entity.ParkingLot;
import com.parkease.parkinglotservice.repository.ParkingLotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingLotService {

    @Autowired
    private ParkingLotRepository repo;

    public ParkingLot create(ParkingLot lot) {
        return repo.save(lot);
    }

    public List<ParkingLot> getAll() {
        return repo.findAll();
    }
}
