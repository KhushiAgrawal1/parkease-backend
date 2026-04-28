package com.parkease.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String message) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("ParkEase Booking Confirmation");
        mail.setText(message);

        mailSender.send(mail);
    }
}