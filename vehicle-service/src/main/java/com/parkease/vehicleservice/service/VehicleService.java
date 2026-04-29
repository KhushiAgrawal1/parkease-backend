package com.parkease.vehicleservice.service;

import com.parkease.vehicleservice.entity.Vehicle;
import com.parkease.vehicleservice.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository repo;

    public Vehicle addVehicle(Vehicle vehicle) {
        return repo.save(vehicle);
    }

    public List<Vehicle> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Vehicle getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }
}