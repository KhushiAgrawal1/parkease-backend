package com.parkease.bookingservice.service;

import com.parkease.bookingservice.client.*;
import com.parkease.bookingservice.dto.ParkingSpot;
import com.parkease.bookingservice.entity.Booking;
import com.parkease.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repo;

    @Mock
    private SpotClient spotClient;

    @Mock
    private LotClient lotClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private ParkingSpot testSpot;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUserId(10L);
        testBooking.setVehicleId(100L);
        testBooking.setSpotId(50L);
        testBooking.setStatus(Booking.BookingStatus.RESERVED);
        testBooking.setBookingType(Booking.BookingType.PRE_BOOKING);
        testBooking.setStartTime(LocalDateTime.now().plusMinutes(30));

        testSpot = new ParkingSpot();
        testSpot.setId(50L);
        testSpot.setStatus("AVAILABLE");
        testSpot.setPricePerHour(50.0);
        testSpot.setLotId(5L);
    }

    @Test
    void testGetAllBookings() {
        when(repo.findAll()).thenReturn(Arrays.asList(testBooking));
        List<Booking> bookings = bookingService.getAll();
        assertEquals(1, bookings.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void testCreateBookingSuccess() {
        when(vehicleClient.getVehicle(100L)).thenReturn(new Object());
        when(spotClient.getSpot(50L)).thenReturn(testSpot);
        when(repo.findBySpotIdAndStatusIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        when(repo.save(any(Booking.class))).thenReturn(testBooking);

        Booking created = bookingService.create(testBooking);

        assertNotNull(created);
        assertEquals(Booking.BookingStatus.RESERVED, created.getStatus());
        verify(spotClient, times(1)).occupySpot(50L);
        verify(repo, times(1)).save(testBooking);
    }

    @Test
    void testCreateBookingSpotOccupied() {
        testSpot.setStatus("OCCUPIED");
        when(vehicleClient.getVehicle(100L)).thenReturn(new Object());
        when(spotClient.getSpot(50L)).thenReturn(testSpot);

        Exception exception = assertThrows(RuntimeException.class, () -> bookingService.create(testBooking));
        assertEquals("Spot already occupied", exception.getMessage());
    }

    @Test
    void testCheckIn() {
        when(repo.findById(1L)).thenReturn(Optional.of(testBooking));
        when(repo.save(any(Booking.class))).thenReturn(testBooking);

        Booking checkedIn = bookingService.checkIn(1L);

        assertEquals(Booking.BookingStatus.ACTIVE, checkedIn.getStatus());
        assertNotNull(checkedIn.getActualCheckInTime());
        verify(repo, times(1)).save(testBooking);
    }

    @Test
    void testCheckOut() {
        testBooking.setStatus(Booking.BookingStatus.ACTIVE);
        testBooking.setActualCheckInTime(LocalDateTime.now().minusHours(2));
        
        when(repo.findById(1L)).thenReturn(Optional.of(testBooking));
        when(spotClient.getSpot(50L)).thenReturn(testSpot);
        when(repo.save(any(Booking.class))).thenReturn(testBooking);

        Booking checkedOut = bookingService.checkOut(1L);

        assertEquals(Booking.BookingStatus.PENDING_PAYMENT, checkedOut.getStatus());
        assertNotNull(checkedOut.getActualCheckOutTime());
        verify(spotClient, times(1)).freeSpot(50L);
        verify(repo, times(1)).save(testBooking);
    }

    @Test
    void testCancel() {
        when(repo.findById(1L)).thenReturn(Optional.of(testBooking));
        when(repo.save(any(Booking.class))).thenReturn(testBooking);

        Booking cancelled = bookingService.cancel(1L);

        assertEquals(Booking.BookingStatus.CANCELLED, cancelled.getStatus());
        verify(spotClient, times(1)).freeSpot(50L);
        verify(paymentClient, times(1)).refundPayment(1L);
        verify(repo, times(1)).save(testBooking);
    }
}
