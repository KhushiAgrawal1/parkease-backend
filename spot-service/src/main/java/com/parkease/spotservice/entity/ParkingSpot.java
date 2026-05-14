package com.parkease.spotservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ParkingSpot {

    @Id
    @GeneratedValue
    private Long id;

    private Long lotId;   // link with ParkingLot
    private String spotNumber;
    private String floor;
    private String status; // AVAILABLE / RESERVED / OCCUPIED
    
    private String spotType; // COMPACT/STANDARD/LARGE/MOTORBIKE/EV
    private String vehicleType; // 2W/4W/HEAVY
    
    private boolean isHandicapped = false;
    private boolean isEVCharging = false;
    
    private Double pricePerHour;
}
