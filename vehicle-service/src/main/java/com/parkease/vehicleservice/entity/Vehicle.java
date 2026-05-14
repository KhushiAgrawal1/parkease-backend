package com.parkease.vehicleservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private String licensePlate;
    
    private String make;
    private String model;

    private String type; // 2W / 4W / HEAVY

    private boolean isEV;
}