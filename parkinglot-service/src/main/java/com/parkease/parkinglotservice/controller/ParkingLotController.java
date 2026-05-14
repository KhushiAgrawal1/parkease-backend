package com.parkease.parkinglotservice.controller;

import com.parkease.parkinglotservice.entity.ParkingLot;
import com.parkease.parkinglotservice.entity.Review;
import com.parkease.parkinglotservice.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lots")
public class ParkingLotController {

    @Autowired
    private ParkingLotService service;

    @PostMapping
    public ParkingLot create(@RequestBody ParkingLot lot) {
        return service.create(lot);
    }

    @GetMapping
    public List<ParkingLot> getAllApproved() {
        return service.getAllApproved();
    }

    @GetMapping("/admin/all")
    public List<ParkingLot> getAllApprovedAdmin() {
        return service.getAllApprovedAdmin();
    }

    @GetMapping("/search")
    public List<ParkingLot> search(@RequestParam(required = false) String query,
                                   @RequestParam(required = false) Double lat,
                                   @RequestParam(required = false) Double lng,
                                   @RequestParam(required = false, defaultValue = "5.0") Double radius) {
        return service.searchAndFilter(query, lat, lng, radius);
    }

    @GetMapping("/pending")
    public List<ParkingLot> getAllPending() {
        return service.getAllPending();
    }

    @GetMapping("/manager/{managerId}")
    public List<ParkingLot> getByManager(@PathVariable Long managerId) {
        return service.getByManager(managerId);
    }

    @PutMapping("/{id}/approve")
    public ParkingLot approveLot(@PathVariable Long id) {
        return service.approve(id);
    }

    @PutMapping("/{id}/toggle")
    public ParkingLot toggleOpen(@PathVariable Long id) {
        return service.toggleOpen(id);
    }

    @PutMapping("/{id}/surge")
    public ParkingLot toggleSurge(@PathVariable Long id) {
        return service.toggleSurge(id);
    }

    @PutMapping("/{id}/suspend")
    public ParkingLot suspendLot(@PathVariable Long id) {
        return service.suspend(id);
    }

    @GetMapping("/{id}")
    public ParkingLot getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/reviews")
    public Review addReview(@PathVariable Long id, @RequestBody Review review) {
        review.setLotId(id);
        return service.addReview(review);
    }

    @GetMapping("/{id}/reviews")
    public List<Review> getReviews(@PathVariable Long id) {
        return service.getReviews(id);
    }
}
