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

    private static final java.util.regex.Pattern LICENSE_PLATE_PATTERN = 
        java.util.regex.Pattern.compile("^[A-Z]{2}[ -]?[0-9]{2}[ -]?[A-Z]{1,2}[ -]?[0-9]{4}$");

    public Vehicle addVehicle(Vehicle vehicle) {
        if (vehicle.getLicensePlate() == null || !LICENSE_PLATE_PATTERN.matcher(vehicle.getLicensePlate().toUpperCase()).matches()) {
            throw new IllegalArgumentException("Invalid license plate format. Must follow standard Indian format (e.g., MH 12 AB 1234).");
        }
        vehicle.setLicensePlate(vehicle.getLicensePlate().toUpperCase()); // Enforce uppercase
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

    public Vehicle update(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getById(id);
        
        if (vehicleDetails.getLicensePlate() != null) {
            if (!LICENSE_PLATE_PATTERN.matcher(vehicleDetails.getLicensePlate().toUpperCase()).matches()) {
                throw new IllegalArgumentException("Invalid license plate format.");
            }
            vehicle.setLicensePlate(vehicleDetails.getLicensePlate().toUpperCase());
        }
        
        if (vehicleDetails.getMake() != null) vehicle.setMake(vehicleDetails.getMake());
        if (vehicleDetails.getModel() != null) vehicle.setModel(vehicleDetails.getModel());
        if (vehicleDetails.getType() != null) vehicle.setType(vehicleDetails.getType());
        vehicle.setEV(vehicleDetails.isEV());
        
        return repo.save(vehicle);
    }
}