package com.parkease.notificationservice.service;

import com.parkease.notificationservice.dto.NotificationRequest;
import com.parkease.notificationservice.entity.Notification;
import com.parkease.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(10L);
        testNotification.setMessage("Test Notification");
        testNotification.setEmail("test@example.com");
    }

    @Test
    void testSend() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(10L);
        request.setMessage("Test Notification");
        request.setEmail("test@example.com");

        when(repo.save(any(Notification.class))).thenReturn(testNotification);

        String result = notificationService.send(request);

        assertEquals("Notification Sent Successfully", result);
        verify(repo, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetByUser() {
        when(repo.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(Collections.singletonList(testNotification));

        List<Notification> notifications = notificationService.getByUser(10L);

        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(10L, notifications.get(0).getUserId());
        verify(repo, times(1)).findByUserIdOrderByCreatedAtDesc(10L);
    }

    @Test
    void testGetByEmail() {
        when(repo.findByEmailOrderByCreatedAtDesc("test@example.com")).thenReturn(Collections.singletonList(testNotification));

        List<Notification> notifications = notificationService.getByEmail("test@example.com");

        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals("test@example.com", notifications.get(0).getEmail());
        verify(repo, times(1)).findByEmailOrderByCreatedAtDesc("test@example.com");
    }
}
