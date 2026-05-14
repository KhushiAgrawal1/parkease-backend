package com.parkease.notificationservice.service;

import com.parkease.notificationservice.dto.NotificationRequest;
import com.parkease.notificationservice.entity.Notification;
import com.parkease.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository repo;

    public String send(NotificationRequest request) {
        log.info("📩 Sending notification to user {}: {}", request.getUserId(), request.getMessage());

        Notification notif = new Notification();
        notif.setUserId(request.getUserId());
        notif.setEmail(request.getEmail());
        notif.setMessage(request.getMessage());
        repo.save(notif);

        return "Notification Sent Successfully";
    }

    public List<Notification> getByUser(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getByEmail(String email) {
        return repo.findByEmailOrderByCreatedAtDesc(email);
    }
}