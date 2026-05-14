package com.parkease.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner seedAdmin(com.parkease.authservice.repository.UserRepository repo, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder) {
        return args -> {
            if (repo.findFirstByEmail("admin@parkease.com").isEmpty()) {
                com.parkease.authservice.entity.User admin = new com.parkease.authservice.entity.User();
                admin.setEmail("admin@parkease.com");
                admin.setFullName("Super Admin");
                String adminPass = "P@rkEase_Adm1n_2026!";
                admin.setPassword(encoder.encode(adminPass)); // Default admin password
                admin.setRole(com.parkease.authservice.entity.User.Role.ADMIN);
                repo.save(admin);
                System.out.println("✅ Super Admin Account Created: admin@parkease.com / P@rkEase_Adm1n_2026!");
            }
        };
    }

    @org.springframework.context.annotation.Bean
    @org.springframework.cloud.client.loadbalancer.LoadBalanced
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }
}
