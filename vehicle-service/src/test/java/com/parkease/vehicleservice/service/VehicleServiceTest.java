package com.parkease.vehicleservice.service;

import com.parkease.vehicleservice.entity.Vehicle;
import com.parkease.vehicleservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository repo;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setLicensePlate("MH 12 AB 1234");
        testVehicle.setUserId(5L);
    }

    @Test
    void testAddVehicle() {
        when(repo.save(any(Vehicle.class))).thenReturn(testVehicle);

        Vehicle addedVehicle = vehicleService.addVehicle(testVehicle);

        assertNotNull(addedVehicle);
        assertEquals("MH 12 AB 1234", addedVehicle.getLicensePlate());
        verify(repo, times(1)).save(testVehicle);
    }

    @Test
    void testGetByUser() {
        when(repo.findByUserId(5L)).thenReturn(Arrays.asList(testVehicle));

        List<Vehicle> vehicles = vehicleService.getByUser(5L);

        assertEquals(1, vehicles.size());
        assertEquals(5L, vehicles.get(0).getUserId());
        verify(repo, times(1)).findByUserId(5L);
    }
    @Test
    void testDeleteVehicle() {
        doNothing().when(repo).deleteById(1L);
        vehicleService.delete(1L);
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void testGetById() {
        when(repo.findById(1L)).thenReturn(java.util.Optional.of(testVehicle));
        Vehicle vehicle = vehicleService.getById(1L);
        assertNotNull(vehicle);
        assertEquals(1L, vehicle.getId());
    }

    @Test
    void testGetByIdNotFound() {
        when(repo.findById(99L)).thenReturn(java.util.Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleService.getById(99L));
        assertEquals("Vehicle not found", exception.getMessage());
    }
}
