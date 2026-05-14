package com.parkease.parkinglotservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Review {

    @Id
    @GeneratedValue
    private Long id;

    private Long lotId;
    private Long userId;
    
    private int rating;
    private String comment;
}
