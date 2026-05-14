package com.parkease.authservice.service;

import com.parkease.authservice.entity.User;
import com.parkease.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("TestP@ssw0rd123!_SECURE"); // Strong password for Regex validation
        testUser.setFullName("Test User");
        testUser.setRole(User.Role.DRIVER);
    }

    @Test
    void testRegisterSuccess() {
        when(repo.findByEmail(testUser.getEmail())).thenReturn(null);
        when(encoder.encode(testUser.getPassword())).thenReturn("hashedPassword");
        when(repo.save(testUser)).thenReturn(testUser);

        User registeredUser = userService.register(testUser);

        assertNotNull(registeredUser);
        assertEquals(User.Role.DRIVER, testUser.getRole());
        assertEquals("hashedPassword", testUser.getPassword());
        verify(repo, times(2)).save(testUser); // once for user, once for OTP
    }

    @Test
    void testLoginSuccess() {
        testUser.setActive(true);
        when(repo.findByEmail(testUser.getEmail())).thenReturn(testUser);
        when(encoder.matches("TestP@ssw0rd123!_SECURE", testUser.getPassword())).thenReturn(true);

        User loggedInUser = userService.login(testUser.getEmail(), "TestP@ssw0rd123!_SECURE");

        assertNotNull(loggedInUser);
        assertEquals(testUser.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void testLoginFailureInvalidPassword() {
        testUser.setActive(true);
        when(repo.findByEmail(testUser.getEmail())).thenReturn(testUser);
        when(encoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(testUser.getEmail(), "wrongpassword");
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }


    @Test
    void testGetAllUsers() {
        when(repo.findAll()).thenReturn(java.util.Collections.singletonList(testUser));

        java.util.List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void testUpdateUserRole() {
        when(repo.findById(1L)).thenReturn(Optional.of(testUser));
        when(repo.save(testUser)).thenReturn(testUser);

        User updatedUser = userService.updateUserRole(1L, User.Role.MANAGER);

        assertEquals(User.Role.MANAGER, testUser.getRole());
        verify(repo, times(1)).save(testUser);
    }
}
