package com.parkease.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("message", ex.getMessage());
        error.put("status", 400);
        error.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
