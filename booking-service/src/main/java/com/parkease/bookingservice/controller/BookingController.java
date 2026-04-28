package com.parkease.bookingservice.controller;

import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService service;

    @PostMapping
    public Booking create(@Valid @RequestBody Booking booking) {
        return service.create(booking);
    }

    @GetMapping
    public List<Booking> getAll() {
        return service.getAll();
    }

    @PutMapping("/cancel/{id}")
    public Booking cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<Booking> getByStatus(@PathVariable String status) {
        return service.getByStatus(status);
    }


}
