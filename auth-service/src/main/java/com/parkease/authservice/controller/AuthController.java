package com.parkease.authservice.controller;
import com.parkease.authservice.entity.User;
import com.parkease.authservice.service.UserService;
import com.parkease.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {

        String email = req.get("username");   // frontend sending username
        String password = req.get("password");

        User user = userService.login(email, password);

        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of("token", token);
    }
}