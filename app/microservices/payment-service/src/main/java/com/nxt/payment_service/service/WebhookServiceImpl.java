package com.nxt.payment_service.service;

import com.nxt.payment_service.entity.PaymentTransaction;
import com.nxt.payment_service.repo.PaymentTransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class WebhookServiceImpl implements WebhookService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final String razorpayWebhookSecret;
    private final String stripeWebhookSecret;

    public WebhookServiceImpl(PaymentTransactionRepository paymentTransactionRepository,
                              PaymentEventPublisher paymentEventPublisher,
                              @Value("${nxt.razorpay.webhook.secret}") String razorpayWebhookSecret,
                              @Value("${nxt.stripe.webhook.secret}") String stripeWebhookSecret) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.razorpayWebhookSecret = razorpayWebhookSecret;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    @Override
    public boolean verifyRazorpaySignature(String payload, String actualSignature) {
        if (payload == null || actualSignature == null || razorpayWebhookSecret == null || razorpayWebhookSecret.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    actualSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean verifyStripeSignature(String payload, String sigHeader) {
        if (payload == null || sigHeader == null || stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            return false;
        }
        try {
            Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            return true;
        } catch (SignatureVerificationException e) {
            return false;
        }
    }

    @Override
    public void processStripeEvent(String payload) {
        markPaidFromPayload(payload, "STRIPE");
    }

    @Override
    public void processRazorpayEvent(String payload) {
        markPaidFromPayload(payload, "RAZORPAY");
    }

    private void markPaidFromPayload(String payload, String gateway) {
        for (PaymentTransaction transaction : paymentTransactionRepository.findByStatus("PENDING")) {
            if (transaction.getOrderId() != null && payload != null && payload.contains(transaction.getOrderId())) {
                transaction.setStatus("PAID");
                transaction.setGateway(gateway);
                transaction.setReceiptId("rcpt-" + transaction.getId());
                paymentTransactionRepository.save(transaction);
                paymentEventPublisher.paymentConfirmed(transaction);
            }
        }
    }
}
