package com.parkease.paymentservice.service;

import com.parkease.paymentservice.entity.Payment;
import com.parkease.paymentservice.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repo;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // 🔥 CREATE RAZORPAY ORDER
    public Payment createOrder(Payment payment) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (payment.getAmount() * 100)); // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(orderRequest);

            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setRazorpayOrderId(order.get("id"));
            payment.setTransactionId(order.get("receipt"));

            return repo.save(payment);
        } catch (RazorpayException e) {
            throw new RuntimeException("Error creating Razorpay Order: " + e.getMessage());
        }
    }

    // 🔥 VERIFY RAZORPAY PAYMENT
    public Payment verifyPayment(Map<String, String> data) {
        String razorpayOrderId = data.get("razorpay_order_id");
        String razorpayPaymentId = data.get("razorpay_payment_id");
        String razorpaySignature = data.get("razorpay_signature");

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean status = Utils.verifyPaymentSignature(options, keySecret);

            if (status) {
                Payment payment = repo.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                payment.setStatus(Payment.PaymentStatus.PAID);
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setMode(Payment.PaymentMode.CARD); // Default or inferred
                payment.setPaidAt(java.time.LocalDateTime.now());

                return repo.save(payment);
            } else {
                throw new RuntimeException("Payment signature verification failed");
            }
        } catch (RazorpayException e) {
            throw new RuntimeException("Error verifying Razorpay Payment: " + e.getMessage());
        }
    }

    // 🔥 GET BY BOOKING
    public Payment getByBookingId(Long bookingId) {

        List<Payment> payments = repo.findByBookingId(bookingId);

        if (payments.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        return payments.get(payments.size() - 1); // latest payment
    }

    // 🔥 REFUND (FIXED)
    public Payment refundPayment(Long bookingId) {

        List<Payment> payments = repo.findByBookingId(bookingId);

        if (payments.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        Payment payment = payments.get(payments.size() - 1); // latest

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Cannot refund unpaid payment");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(java.time.LocalDateTime.now());

        return repo.save(payment);
    }

    // 🔥 PAYMENT HISTORY
    public List<Payment> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }
}