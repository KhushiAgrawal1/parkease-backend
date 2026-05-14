package com.parkease.spotservice.service;

import com.parkease.spotservice.entity.ParkingSpot;
import com.parkease.spotservice.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpotService {

    @Autowired
    private SpotRepository repo;

    public ParkingSpot create(ParkingSpot spot) {
        if (repo.existsByLotIdAndSpotNumber(spot.getLotId(), spot.getSpotNumber())) {
            throw new RuntimeException("Spot number already exists in this lot");
        }
        spot.setStatus("AVAILABLE");
        return repo.save(spot);
    }

    public List<ParkingSpot> getByLot(Long lotId) {
        return repo.findByLotId(lotId);
    }

    public ParkingSpot occupySpot(Long id) {
        ParkingSpot spot = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        spot.setStatus("OCCUPIED");
        return repo.save(spot);
    }

    public ParkingSpot getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found"));
    }

    public ParkingSpot freeSpot(Long id) {
        ParkingSpot spot = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        spot.setStatus("AVAILABLE");
        return repo.save(spot);
    }

    public List<ParkingSpot> getAvailableSpots(Long lotId) {
        return repo.findByLotIdAndStatus(lotId, "AVAILABLE");
    }

    public ParkingSpot update(Long id, ParkingSpot spotDetails) {
        ParkingSpot spot = repo.findById(id).orElseThrow(() -> new RuntimeException("Spot not found"));
        spot.setSpotNumber(spotDetails.getSpotNumber());
        spot.setFloor(spotDetails.getFloor());
        spot.setSpotType(spotDetails.getSpotType());
        spot.setVehicleType(spotDetails.getVehicleType());
        spot.setHandicapped(spotDetails.isHandicapped());
        spot.setEVCharging(spotDetails.isEVCharging());
        spot.setPricePerHour(spotDetails.getPricePerHour());
        return repo.save(spot);
    }

    public List<ParkingSpot> createBulk(List<ParkingSpot> spots) {
        spots.forEach(spot -> {
            if (repo.existsByLotIdAndSpotNumber(spot.getLotId(), spot.getSpotNumber())) {
                throw new RuntimeException("Spot number " + spot.getSpotNumber() + " already exists in this lot");
            }
            spot.setStatus("AVAILABLE");
        });
        return repo.saveAll(spots);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
