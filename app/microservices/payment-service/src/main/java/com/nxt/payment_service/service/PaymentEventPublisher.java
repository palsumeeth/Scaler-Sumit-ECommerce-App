package com.nxt.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.payment_service.entity.PaymentTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class PaymentEventPublisher {

    public static final String PAYMENT_CONFIRMED_TOPIC = "payment.confirmed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void paymentConfirmed(PaymentTransaction transaction) {
        String email = transaction.getEmail();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "PAYMENT_CONFIRMED");
        event.put("orderId", transaction.getOrderId());
        event.put("receiptId", transaction.getReceiptId());
        event.put("amount", transaction.getAmount());
        event.put("gateway", transaction.getGateway());
        event.put("status", transaction.getStatus());
        event.put("email", email);
        send(PAYMENT_CONFIRMED_TOPIC, event);
    }

    private void send(String topic, Object payload) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("Could not publish to {}: {}", topic, e.getMessage());
        }
    }
}
