package com.parkease.bookingservice.dto;

import com.parkease.bookingservice.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingDetailsResponse {

    private Booking booking;
    private Object spot;
    private Object vehicle;
    private PaymentResponse payment; // ✅ FIXED
}