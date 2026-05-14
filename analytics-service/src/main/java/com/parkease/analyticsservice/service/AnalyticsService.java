package com.parkease.analyticsservice.service;

import com.parkease.analyticsservice.client.BookingClient;
import com.parkease.analyticsservice.client.LotClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private LotClient lotClient;

    public Map<String, Object> getPlatformAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            Map<String, Object> bookingData = bookingClient.getAnalytics();
            analytics.putAll(bookingData);
        } catch (Exception e) {
            analytics.put("totalBookings", 0);
            analytics.put("activeBookings", 0);
            analytics.put("totalRevenue", 0);
        }

        try {
            List<Object> lots = lotClient.getAllLots();
            analytics.put("totalLots", lots.size());
        } catch (Exception e) {
            analytics.put("totalLots", 0);
        }

        return analytics;
    }
}
