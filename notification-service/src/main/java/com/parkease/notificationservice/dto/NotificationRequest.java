package com.parkease.notificationservice.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private String message;
    private Long userId;

    // getters & setters
}