package com.parkease.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsResponse {

    private long totalBookings;
    private long activeBookings;
    private double totalRevenue;
}