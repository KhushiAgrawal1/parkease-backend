package com.parkease.authservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class User implements java.io.Serializable {
    public enum Role {
        DRIVER,
        MANAGER,
        ADMIN
    }
    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String password;
    
    private String fullName;
    private String phone;
    
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private Role role;
    
    private String vehiclePlate;
    private boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String profilePicUrl;
    
    private String otp;
    private LocalDateTime otpExpiry;
}