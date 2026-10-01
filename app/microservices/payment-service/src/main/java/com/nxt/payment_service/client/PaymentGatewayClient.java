package com.nxt.payment_service.client;

public interface PaymentGatewayClient {

    String getPaymentLink(Long amount, String orderId, String phoneNumber, String name); 
}
