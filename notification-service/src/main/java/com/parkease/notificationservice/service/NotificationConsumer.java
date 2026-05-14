package com.parkease.notificationservice.service;

import com.parkease.notificationservice.config.RabbitMQConfig;
import com.parkease.notificationservice.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeNotification(Map<String, Object> messageMap) {
        log.info("Received message from RabbitMQ: {}", messageMap);
        
        try {
            NotificationRequest request = new NotificationRequest();
            if (messageMap.containsKey("email")) {
                request.setEmail((String) messageMap.get("email"));
            }
            if (messageMap.containsKey("message")) {
                request.setMessage((String) messageMap.get("message"));
            }
            if (messageMap.containsKey("userId")) {
                Object userIdObj = messageMap.get("userId");
                if (userIdObj instanceof Integer) {
                    request.setUserId(((Integer) userIdObj).longValue());
                } else if (userIdObj instanceof Long) {
                    request.setUserId((Long) userIdObj);
                } else if (userIdObj != null) {
                    request.setUserId(Long.valueOf(userIdObj.toString()));
                }
            }

            notificationService.send(request);
            
            // Optionally, send an actual email if email is present
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                emailService.sendEmail(request.getEmail(), "ParkEase Notification", request.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to process notification message", e);
        }
    }
}
