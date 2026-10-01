package com.nxt.payment_service.service;

public interface WebhookService {
    boolean verifyRazorpaySignature(String payload, String actualSignature);
    boolean verifyStripeSignature(String payload, String signatureHeader);
    void processRazorpayEvent(String payload);
    void processStripeEvent(String payload);
}
