package com.parkease.parkinglotservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ParkingLot {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String city;
    private String address;
    private String landmark;
    private Double latitude;
    private Double longitude;
    
    private Long managerId;
    private boolean adminApproved = false;
    
    private int totalSpots;
    private int availableSpots;
    
    private boolean isSurgeActive = false;
    private Double averageRating = 0.0;
    
    private boolean isOpen = true;
    private String openTime;
    private String closeTime;
}
