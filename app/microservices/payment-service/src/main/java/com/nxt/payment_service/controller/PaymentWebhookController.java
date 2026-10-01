package com.nxt.payment_service.controller;

import com.nxt.payment_service.service.WebhookService;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/webhook")
public class PaymentWebhookController {

    private final WebhookService webhookService;

    @Autowired
    public PaymentWebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request,
                                                      @RequestHeader("Stripe-Signature") String signatureHeader) {

        String payload = processHttpServletRequest(request);

        if (!webhookService.verifyStripeSignature(payload, signatureHeader))
            return ResponseEntity.status(400).body("Invalid Stripe signature");

        webhookService.processStripeEvent(payload);
        return ResponseEntity.ok("Received Stripe webhook");
    }

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(HttpServletRequest request,
                                                        @RequestHeader("X-Razorpay-Signature") String razorpaySignature) {

        String payload = processHttpServletRequest(request);

        if (!webhookService.verifyRazorpaySignature(payload, razorpaySignature)) {
            return ResponseEntity.status(400).body("Invalid Razorpay signature");
        }

        webhookService.processRazorpayEvent(payload);
        return ResponseEntity.ok("Received Razorpay webhook");
    }


    private String processHttpServletRequest(HttpServletRequest request) {
        ServletInputStream inputStream;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String payload;
        try {
            payload = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return payload;
    }
}