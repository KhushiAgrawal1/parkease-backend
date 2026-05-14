package com.parkease.parkinglotservice.service;

import com.parkease.parkinglotservice.entity.ParkingLot;
import com.parkease.parkinglotservice.entity.Review;
import com.parkease.parkinglotservice.repository.ParkingLotRepository;
import com.parkease.parkinglotservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class ParkingLotService {

    @Autowired
    private ParkingLotRepository repo;

    @Autowired
    private ReviewRepository reviewRepo;

    public ParkingLot create(ParkingLot lot) {
        lot.setAdminApproved(false); // require approval
        lot.setOpen(false); // closed by default until approved
        return repo.save(lot);
    }

    public List<ParkingLot> getAllApproved() {
        return repo.findByAdminApprovedTrue();
    }

    public List<ParkingLot> getAllApprovedAdmin() {
        return repo.findByAdminApprovedTrue();
    }

    public List<ParkingLot> getAllPending() {
        return repo.findByAdminApprovedFalse();
    }

    public List<ParkingLot> getByManager(Long managerId) {
        return repo.findByManagerId(managerId);
    }

    public ParkingLot approve(Long id) {
        ParkingLot lot = repo.findById(id).orElseThrow(() -> new RuntimeException("Lot not found"));
        lot.setAdminApproved(true);
        return repo.save(lot);
    }
    
    public ParkingLot toggleOpen(Long id) {
        ParkingLot lot = repo.findById(id).orElseThrow(() -> new RuntimeException("Lot not found"));
        lot.setOpen(!lot.isOpen());
        return repo.save(lot);
    }
    
    public ParkingLot toggleSurge(Long id) {
        ParkingLot lot = repo.findById(id).orElseThrow(() -> new RuntimeException("Lot not found"));
        lot.setSurgeActive(!lot.isSurgeActive());
        return repo.save(lot);
    }

    public ParkingLot suspend(Long id) {
        ParkingLot lot = repo.findById(id).orElseThrow(() -> new RuntimeException("Lot not found"));
        lot.setAdminApproved(false);
        return repo.save(lot);
    }
    
    public ParkingLot getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Lot not found"));
    }

    public Review addReview(Review review) {
        Review saved = reviewRepo.save(review);
        updateAverageRating(review.getLotId());
        return saved;
    }

    public List<Review> getReviews(Long lotId) {
        return reviewRepo.findByLotId(lotId);
    }

    private void updateAverageRating(Long lotId) {
        ParkingLot lot = getById(lotId);
        List<Review> reviews = reviewRepo.findByLotId(lotId);
        if (reviews.isEmpty()) {
            lot.setAverageRating(0.0);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            lot.setAverageRating(avg);
        }
        repo.save(lot);
    }

    public List<ParkingLot> searchAndFilter(String query, Double lat, Double lng, Double radius) {
        List<ParkingLot> lots;
        if (query != null && !query.trim().isEmpty()) {
            lots = repo.searchLots(query.trim());
        } else {
            lots = getAllApproved(); // Returns all admin-approved lots
        }

        if (lat != null && lng != null && radius != null) {
            lots = lots.stream()
                .filter(lot -> lot.getLatitude() != null && lot.getLongitude() != null)
                .filter(lot -> calculateDistance(lat, lng, lot.getLatitude(), lot.getLongitude()) <= radius)
                .sorted(Comparator.comparingDouble(lot -> calculateDistance(lat, lng, lot.getLatitude(), lot.getLongitude())))
                .collect(Collectors.toList());
        }

        return lots;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }
}
