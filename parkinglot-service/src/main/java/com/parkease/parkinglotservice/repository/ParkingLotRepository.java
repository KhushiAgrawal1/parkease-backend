package com.parkease.parkinglotservice.repository;

import com.parkease.parkinglotservice.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
}
