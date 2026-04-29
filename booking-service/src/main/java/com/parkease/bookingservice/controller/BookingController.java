package com.parkease.bookingservice.controller;

import com.parkease.bookingservice.dto.AnalyticsResponse;
import com.parkease.bookingservice.dto.BookingDetailsResponse;
import com.parkease.bookingservice.dto.RecentBookingDTO;
import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService service;

    @PostMapping
    public Booking create(@RequestBody Booking booking) {
        return service.create(booking);
    }

    @GetMapping
    public List<Booking> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @PutMapping("/{id}/checkin")
    public Booking checkIn(@PathVariable Long id) {
        return service.checkIn(id);
    }

    @PutMapping("/{id}/checkout")
    public Booking checkOut(@PathVariable Long id) {
        return service.checkOut(id);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PutMapping("/{id}/retry-payment")
    public Booking retryPayment(@PathVariable Long id) {
        return service.retryPayment(id);
    }

    @GetMapping("/analytics")
    public AnalyticsResponse analytics() {
        return service.getAnalytics();
    }

    @GetMapping("/status-count")
    public Map<String, Long> getStatusCount() {
        return service.getStatusCount();
    }

    @GetMapping("/{id}/details")
    public BookingDetailsResponse getDetails(@PathVariable Long id) {
        return service.getBookingDetails(id);
    }

    @GetMapping("/recent/{userId}")
    public List<RecentBookingDTO> getRecent(@PathVariable Long userId) {
        return service.getRecentBookings(userId);
    }
}