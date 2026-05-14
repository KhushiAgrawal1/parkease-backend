package com.parkease.notificationservice.controller;

import com.parkease.notificationservice.dto.NotificationRequest;
import com.parkease.notificationservice.service.EmailService;
import com.parkease.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import com.parkease.notificationservice.entity.Notification;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public void sendNotification(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String message = req.get("message");
        String subject = req.getOrDefault("subject", "ParkEase Notification");
        String userIdStr = req.get("userId");

        // Save to DB
        NotificationRequest notifReq = new NotificationRequest();
        notifReq.setMessage(message);
        notifReq.setEmail(email);
        if (userIdStr != null) {
            notifReq.setUserId(Long.parseLong(userIdStr));
        }
        notificationService.send(notifReq);

        // Send Email
        if (email != null && !email.isEmpty()) {
            emailService.sendEmail(email, subject, message);
        }
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable Long userId) {
        return notificationService.getByUser(userId);
    }
}