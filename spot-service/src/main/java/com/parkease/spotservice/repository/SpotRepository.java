package com.parkease.spotservice.repository;

import com.parkease.spotservice.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByLotId(Long lotId);
    List<ParkingSpot> findByLotIdAndStatus(Long lotId, String status);
}
