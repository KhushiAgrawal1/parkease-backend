package com.parkease.parkinglotservice.service;

import com.parkease.parkinglotservice.entity.ParkingLot;
import com.parkease.parkinglotservice.entity.Review;
import com.parkease.parkinglotservice.repository.ParkingLotRepository;
import com.parkease.parkinglotservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingLotServiceTest {

    @Mock
    private ParkingLotRepository repo;

    @Mock
    private ReviewRepository reviewRepo;

    @InjectMocks
    private ParkingLotService parkingLotService;

    private ParkingLot testLot;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testLot = new ParkingLot();
        testLot.setId(1L);
        testLot.setName("Downtown Lot");
        testLot.setManagerId(10L);
        testLot.setOpen(true);
        testLot.setSurgeActive(false);

        testReview = new Review();
        testReview.setId(100L);
        testReview.setLotId(1L);
        testReview.setRating(5);
        testReview.setComment("Great!");
    }

    @Test
    void testCreateLot() {
        when(repo.save(any(ParkingLot.class))).thenReturn(testLot);

        ParkingLot createdLot = parkingLotService.create(testLot);

        assertNotNull(createdLot);
        assertFalse(createdLot.isAdminApproved());
        verify(repo, times(1)).save(testLot);
    }

    @Test
    void testApproveLot() {
        when(repo.findById(1L)).thenReturn(Optional.of(testLot));
        when(repo.save(any(ParkingLot.class))).thenReturn(testLot);

        ParkingLot approvedLot = parkingLotService.approve(1L);

        assertTrue(approvedLot.isAdminApproved());
        verify(repo, times(1)).save(testLot);
    }

    @Test
    void testGetAllApproved() {
        when(repo.findByAdminApprovedTrueAndIsOpenTrue()).thenReturn(Arrays.asList(testLot));
        List<ParkingLot> lots = parkingLotService.getAllApproved();
        assertEquals(1, lots.size());
    }

    @Test
    void testToggleOpen() {
        when(repo.findById(1L)).thenReturn(Optional.of(testLot));
        when(repo.save(any(ParkingLot.class))).thenReturn(testLot);

        ParkingLot toggled = parkingLotService.toggleOpen(1L);

        assertFalse(toggled.isOpen());
    }

    @Test
    void testToggleSurge() {
        when(repo.findById(1L)).thenReturn(Optional.of(testLot));
        when(repo.save(any(ParkingLot.class))).thenReturn(testLot);

        ParkingLot toggled = parkingLotService.toggleSurge(1L);

        assertTrue(toggled.isSurgeActive());
    }

    @Test
    void testSuspendLot() {
        when(repo.findById(1L)).thenReturn(Optional.of(testLot));
        when(repo.save(any(ParkingLot.class))).thenReturn(testLot);

        ParkingLot suspended = parkingLotService.suspend(1L);

        assertFalse(suspended.isAdminApproved());
    }

    @Test
    void testAddReview() {
        when(reviewRepo.save(any(Review.class))).thenReturn(testReview);
        when(repo.findById(1L)).thenReturn(Optional.of(testLot));
        when(reviewRepo.findByLotId(1L)).thenReturn(Arrays.asList(testReview));
        when(repo.save(any(ParkingLot.class))).thenReturn(testLot);

        Review savedReview = parkingLotService.addReview(testReview);

        assertNotNull(savedReview);
        assertEquals(5, testLot.getAverageRating());
        verify(repo, times(1)).save(testLot);
    }

    @Test
    void testGetReviews() {
        when(reviewRepo.findByLotId(1L)).thenReturn(Arrays.asList(testReview));
        List<Review> reviews = parkingLotService.getReviews(1L);
        assertEquals(1, reviews.size());
    }
}
