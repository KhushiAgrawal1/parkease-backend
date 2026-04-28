package com.parkease.notificationservice.controller;

import com.parkease.notificationservice.dto.NotificationRequest;
import com.parkease.notificationservice.service.EmailService;
import com.parkease.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notify")
public class NotificationController {


    @Autowired
    private EmailService emailService;

    @PostMapping
    public void sendNotification(@RequestBody Map<String, String> req) {

        String email = req.get("email");   // 🔥 FIXED
        String message = req.get("message");

        emailService.sendEmail(email, message);
    }
}