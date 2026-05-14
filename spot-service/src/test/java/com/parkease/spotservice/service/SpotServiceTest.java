package com.parkease.spotservice.service;

import com.parkease.spotservice.entity.ParkingSpot;
import com.parkease.spotservice.repository.SpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotServiceTest {

    @Mock
    private SpotRepository repo;

    @InjectMocks
    private SpotService spotService;

    private ParkingSpot testSpot;

    @BeforeEach
    void setUp() {
        testSpot = new ParkingSpot();
        testSpot.setId(1L);
        testSpot.setSpotNumber("A1");
        testSpot.setLotId(10L);
    }

    @Test
    void testCreateSpot() {
        when(repo.save(any(ParkingSpot.class))).thenReturn(testSpot);

        ParkingSpot createdSpot = spotService.create(testSpot);

        assertNotNull(createdSpot);
        assertEquals("AVAILABLE", testSpot.getStatus());
        verify(repo, times(1)).save(testSpot);
    }

    @Test
    void testOccupySpot() {
        testSpot.setStatus("AVAILABLE");
        when(repo.findById(1L)).thenReturn(Optional.of(testSpot));
        when(repo.save(any(ParkingSpot.class))).thenReturn(testSpot);

        ParkingSpot occupiedSpot = spotService.occupySpot(1L);

        assertEquals("OCCUPIED", occupiedSpot.getStatus());
        verify(repo, times(1)).save(testSpot);
    }
    @Test
    void testGetByLot() {
        when(repo.findByLotId(10L)).thenReturn(java.util.Collections.singletonList(testSpot));
        java.util.List<ParkingSpot> spots = spotService.getByLot(10L);
        assertEquals(1, spots.size());
        verify(repo, times(1)).findByLotId(10L);
    }

    @Test
    void testGetById() {
        when(repo.findById(1L)).thenReturn(Optional.of(testSpot));
        ParkingSpot spot = spotService.getById(1L);
        assertNotNull(spot);
        assertEquals(1L, spot.getId());
    }

    @Test
    void testFreeSpot() {
        testSpot.setStatus("OCCUPIED");
        when(repo.findById(1L)).thenReturn(Optional.of(testSpot));
        when(repo.save(any(ParkingSpot.class))).thenReturn(testSpot);

        ParkingSpot freedSpot = spotService.freeSpot(1L);

        assertEquals("AVAILABLE", freedSpot.getStatus());
        verify(repo, times(1)).save(testSpot);
    }

    @Test
    void testGetAvailableSpots() {
        when(repo.findByLotIdAndStatus(10L, "AVAILABLE")).thenReturn(java.util.Collections.singletonList(testSpot));
        java.util.List<ParkingSpot> spots = spotService.getAvailableSpots(10L);
        assertEquals(1, spots.size());
        verify(repo, times(1)).findByLotIdAndStatus(10L, "AVAILABLE");
    }
}
