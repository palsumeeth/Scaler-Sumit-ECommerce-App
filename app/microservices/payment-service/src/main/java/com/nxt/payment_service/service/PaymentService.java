package com.nxt.payment_service.service;

public interface PaymentService {
    String getPaymentLink(Long amount, String orderId, String phoneNumber, String name);
}
