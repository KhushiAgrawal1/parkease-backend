package com.parkease.authservice.controller;
import com.parkease.authservice.entity.User;
import com.parkease.authservice.service.UserService;
import com.parkease.authservice.dto.UserRegistrationDto;
import com.parkease.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody UserRegistrationDto dto) {
        User savedUser = userService.register(dto.toEntity());
        return Map.of(
            "id", savedUser.getId(),
            "email", savedUser.getEmail(),
            "role", savedUser.getRole()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {

        String email = req.get("username");   // frontend sending username
        String password = req.get("password");

        User user = userService.login(email, password);

        String role = user.getRole() != null ? user.getRole().name() : "DRIVER";
        String token = jwtUtil.generateToken(user.getEmail(), role);

        return Map.of(
            "token", token,
            "role", role,
            "userId", user.getId(),
            "email", user.getEmail()
        );
    }

    @PostMapping("/google")
    public Map<String, Object> googleLogin(@RequestBody Map<String, String> req) {
        String credential = req.get("credential");
        User user = userService.googleLogin(credential);

        String role = user.getRole() != null ? user.getRole().name() : "DRIVER";
        String token = jwtUtil.generateToken(user.getEmail(), role);

        return Map.of(
            "token", token,
            "role", role,
            "userId", user.getId(),
            "email", user.getEmail()
        );
    }

    @PostMapping("/register/verify")
    public Map<String, Object> verifyRegistrationOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");

        userService.verifyRegistrationOtp(email, otp);

        return Map.of("message", "Registration successful! You can now sign in.");
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> userReq) {
        User user = new User();
        user.setFullName(userReq.get("fullName"));
        user.setPhone(userReq.get("phone"));
        User updatedUser = userService.updateProfile(id, user);
        return Map.of(
            "id", updatedUser.getId(),
            "email", updatedUser.getEmail(),
            "fullName", updatedUser.getFullName() != null ? updatedUser.getFullName() : "",
            "phone", updatedUser.getPhone() != null ? updatedUser.getPhone() : ""
        );
    }
}