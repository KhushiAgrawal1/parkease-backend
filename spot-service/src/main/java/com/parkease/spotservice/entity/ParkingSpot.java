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
    private String status; // AVAILABLE / OCCUPIED
}
