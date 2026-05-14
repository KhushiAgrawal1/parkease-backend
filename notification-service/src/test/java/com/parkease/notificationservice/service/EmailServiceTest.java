package com.parkease.notificationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendEmailTriggersJavaMailSender() {
        String to = "user@example.com";
        String subject = "Test Subject";
        String message = "This is a test message.";

        jakarta.mail.internet.MimeMessage mimeMessage = org.mockito.Mockito.mock(jakarta.mail.internet.MimeMessage.class);
        org.mockito.Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(to, subject, message);

        verify(mailSender, times(1)).send(any(jakarta.mail.internet.MimeMessage.class));
    }
}
