package com.parkease.notificationservice.service;

import com.parkease.notificationservice.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public String send(NotificationRequest request) {

        // Simulate sending notification
        log.info("📩 Sending notification to user {}: {}", request.getUserId(), request.getMessage());

        return "Notification Sent Successfully";
    }
}