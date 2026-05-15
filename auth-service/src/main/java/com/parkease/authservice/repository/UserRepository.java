package com.parkease.authservice.repository;

import com.parkease.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findFirstByEmail(String email);

    @Cacheable(value = "users", key = "#p0")
    User findByEmail(String email);

    @CachePut(value = "users", key = "#user.email")
    <S extends User> S save(S user);
}
