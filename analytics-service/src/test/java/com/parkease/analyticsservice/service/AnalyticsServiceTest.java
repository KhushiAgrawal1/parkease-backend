package com.parkease.analyticsservice.service;

import com.parkease.analyticsservice.client.BookingClient;
import com.parkease.analyticsservice.client.LotClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private BookingClient bookingClient;

    @Mock
    private LotClient lotClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testGetPlatformAnalyticsSuccess() {
        Map<String, Object> mockBookingData = new HashMap<>();
        mockBookingData.put("totalBookings", 50);
        mockBookingData.put("totalRevenue", 1000);

        when(bookingClient.getAnalytics()).thenReturn(mockBookingData);
        when(lotClient.getAllLots()).thenReturn(Arrays.asList(new Object(), new Object())); // 2 lots

        Map<String, Object> analytics = analyticsService.getPlatformAnalytics();

        assertNotNull(analytics);
        assertEquals(50, analytics.get("totalBookings"));
        assertEquals(1000, analytics.get("totalRevenue"));
        assertEquals(2, analytics.get("totalLots"));
    }

    @Test
    void testGetPlatformAnalyticsFallback() {
        when(bookingClient.getAnalytics()).thenThrow(new RuntimeException("Service Down"));
        when(lotClient.getAllLots()).thenThrow(new RuntimeException("Service Down"));

        Map<String, Object> analytics = analyticsService.getPlatformAnalytics();

        assertNotNull(analytics);
        assertEquals(0, analytics.get("totalBookings"));
        assertEquals(0, analytics.get("totalLots"));
    }
    @Test
    void testGetPlatformAnalyticsBookingFailsLotSucceeds() {
        when(bookingClient.getAnalytics()).thenThrow(new RuntimeException("Booking Down"));
        when(lotClient.getAllLots()).thenReturn(Arrays.asList(new Object())); // 1 lot

        Map<String, Object> analytics = analyticsService.getPlatformAnalytics();

        assertNotNull(analytics);
        assertEquals(0, analytics.get("totalBookings"));
        assertEquals(0, analytics.get("totalRevenue"));
        assertEquals(1, analytics.get("totalLots"));
    }

    @Test
    void testGetPlatformAnalyticsBookingSucceedsLotFails() {
        Map<String, Object> mockBookingData = new HashMap<>();
        mockBookingData.put("totalBookings", 30);
        mockBookingData.put("totalRevenue", 500);

        when(bookingClient.getAnalytics()).thenReturn(mockBookingData);
        when(lotClient.getAllLots()).thenThrow(new RuntimeException("Lot Down"));

        Map<String, Object> analytics = analyticsService.getPlatformAnalytics();

        assertNotNull(analytics);
        assertEquals(30, analytics.get("totalBookings"));
        assertEquals(500, analytics.get("totalRevenue"));
        assertEquals(0, analytics.get("totalLots"));
    }
}
