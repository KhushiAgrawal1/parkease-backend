package com.parkease.authservice.service;

import com.parkease.authservice.entity.User;
import com.parkease.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final RestTemplate restTemplate;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder, RestTemplate restTemplate) {
        this.repo = repo;
        this.encoder = encoder;
        this.restTemplate = restTemplate;
    }

    private static final java.util.regex.Pattern PASSWORD_PATTERN = 
        java.util.regex.Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*_~-]).{8,}$");

    private static final java.util.regex.Pattern PHONE_PATTERN = 
        java.util.regex.Pattern.compile("^[6-9]\\d{9}$");

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    public User register(User user) {
        if (user.getPassword() == null || !PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }
        
        if (user.getPhone() != null && !user.getPhone().isEmpty() && !PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            throw new IllegalArgumentException("Invalid Indian phone number format.");
        }

        User existingUser = repo.findByEmail(user.getEmail());
        if (existingUser != null) {
            if (existingUser.isActive()) {
                throw new RuntimeException("User already registered with this email.");
            }
            // Overwrite inactive user
            user.setId(existingUser.getId());
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(User.Role.DRIVER); // FORCE Driver role for public registrations
        user.setActive(false);
        User savedUser = repo.save(user);
        generateAndSendOtp(savedUser);
        return savedUser;
    }

    @org.springframework.beans.factory.annotation.Value("${google.client.id}")
    private String googleClientId;

    public User login(String email, String password) {
        User user = repo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account not verified. Please verify your email.");
        }

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User googleLogin(String credential) {
        try {
            com.google.api.client.http.javanet.NetHttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.client.json.gson.GsonFactory jsonFactory = new com.google.api.client.json.gson.GsonFactory();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(credential);
            if (idToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                User user = repo.findByEmail(email);
                if (user == null) {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name);
                    newUser.setPassword(encoder.encode(java.util.UUID.randomUUID().toString())); // Random password
                    newUser.setRole(User.Role.DRIVER);
                    newUser.setActive(true);
                    user = repo.save(newUser);
                }
                
                return user;
            } else {
                throw new RuntimeException("Invalid Google token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed", e);
        }
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);

    private void generateAndSendOtp(User user) {
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        repo.save(user);

        // Send Email via notification-service
        String htmlMessage = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px; background-color: #ffffff;\">"
            + "<div style=\"text-align: center; margin-bottom: 24px;\">"
            + "<h1 style=\"color: #2563eb; margin: 0; font-size: 28px;\">ParkEase</h1>"
            + "<p style=\"color: #64748b; margin-top: 4px; font-size: 14px; letter-spacing: 1px;\">SMART PARKING</p>"
            + "</div>"
            + "<h2 style=\"color: #0f172a; font-size: 20px; margin-bottom: 16px;\">Verify Your Email</h2>"
            + "<p style=\"color: #334155; font-size: 16px; line-height: 1.5; margin-bottom: 24px;\">Hello! Thank you for joining ParkEase. To complete your registration and secure your account, please use the verification code below:</p>"
            + "<div style=\"background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 20px; text-align: center; margin-bottom: 24px;\">"
            + "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #0f172a;\">" + otp + "</span>"
            + "</div>"
            + "<p style=\"color: #64748b; font-size: 14px;\">This code is valid for 10 minutes. If you did not request this verification, please ignore this email.</p>"
            + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;\" />"
            + "<p style=\"color: #94a3b8; font-size: 12px; text-align: center;\">&copy; " + java.time.Year.now().getValue() + " ParkEase Technologies. All rights reserved.</p>"
            + "</div>";

        Map<String, String> request = new HashMap<>();
        request.put("email", user.getEmail());
        request.put("subject", "ParkEase Login Verification");
        request.put("message", htmlMessage);
        request.put("userId", user.getId().toString());

        try {
            restTemplate.postForObject("http://NOTIFICATION-SERVICE/notify", request, Void.class);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
        }
    }

    public User verifyRegistrationOtp(String email, String otp) {
        User user = repo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
                
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        
        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
        
        // Clear OTP and activate user
        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setActive(true);
        return repo.save(user);
    }

    public java.util.List<User> getAllUsers() {
        return repo.findAll();
    }

    public User updateUserRole(Long userId, User.Role newRole) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        return repo.save(user);
    }

    public User updateProfile(Long userId, User updatedData) {
        User user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (updatedData.getFullName() != null) user.setFullName(updatedData.getFullName());
        if (updatedData.getPhone() != null) {
            if (!PHONE_PATTERN.matcher(updatedData.getPhone()).matches()) {
                throw new IllegalArgumentException("Invalid Indian phone number format.");
            }
            user.setPhone(updatedData.getPhone());
        }
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            if (!PASSWORD_PATTERN.matcher(updatedData.getPassword()).matches()) {
                throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
            }
            user.setPassword(encoder.encode(updatedData.getPassword()));
        }
        return repo.save(user);
    }

    public User toggleUserActive(Long userId, boolean isActive) {
        User user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(isActive);
        return repo.save(user);
    }

    public void deleteUser(Long userId) {
        repo.deleteById(userId);
    }
}
