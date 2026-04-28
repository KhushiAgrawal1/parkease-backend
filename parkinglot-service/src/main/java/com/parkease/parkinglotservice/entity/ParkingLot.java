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
    private int totalSpots;
}
