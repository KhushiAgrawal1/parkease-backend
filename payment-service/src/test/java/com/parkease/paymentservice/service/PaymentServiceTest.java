package com.parkease.paymentservice.service;

import com.parkease.paymentservice.entity.Payment;
import com.parkease.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repo;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBookingId(100L);
        testPayment.setStatus(Payment.PaymentStatus.PAID);
    }

    @Test
    void testGetByBookingId() {
        when(repo.findByBookingId(100L)).thenReturn(List.of(testPayment));

        Payment result = paymentService.getByBookingId(100L);

        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.PAID, result.getStatus());
        verify(repo, times(1)).findByBookingId(100L);
    }

    @Test
    void testGetByBookingIdNotFound() {
        when(repo.findByBookingId(999L)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getByBookingId(999L);
        });

        assertEquals("Payment not found", exception.getMessage());
    }
    @Test
    void testRefundPaymentSuccess() {
        when(repo.findByBookingId(100L)).thenReturn(List.of(testPayment));
        when(repo.save(any(Payment.class))).thenReturn(testPayment);

        Payment refunded = paymentService.refundPayment(100L);

        assertEquals(Payment.PaymentStatus.REFUNDED, refunded.getStatus());
        assertNotNull(refunded.getRefundedAt());
        verify(repo, times(1)).save(testPayment);
    }

    @Test
    void testRefundPaymentUnpaid() {
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        when(repo.findByBookingId(100L)).thenReturn(List.of(testPayment));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentService.refundPayment(100L);
        });

        assertEquals("Cannot refund unpaid payment", exception.getMessage());
    }

    @Test
    void testGetByUser() {
        testPayment.setUserId(10L);
        when(repo.findByUserId(10L)).thenReturn(List.of(testPayment));

        List<Payment> payments = paymentService.getByUser(10L);

        assertEquals(1, payments.size());
        assertEquals(10L, payments.get(0).getUserId());
        verify(repo, times(1)).findByUserId(10L);
    }

}
