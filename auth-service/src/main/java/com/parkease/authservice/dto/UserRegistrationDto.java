package com.parkease.authservice.dto;

import com.parkease.authservice.entity.User;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String email;
    private String password;
    private String fullName;
    private User.Role role;

    public User toEntity() {
        User user = new User();
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setFullName(this.fullName);
        user.setRole(this.role != null ? this.role : User.Role.DRIVER);
        return user;
    }
}
