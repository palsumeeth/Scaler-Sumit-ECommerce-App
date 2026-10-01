package com.nxt.payment_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderPlacedListener {

    private final PaymentServiceImpl paymentService;
    private final ObjectMapper objectMapper;

    public OrderPlacedListener(PaymentServiceImpl paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.placed", groupId = "payment-service")
    public void onOrderPlaced(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String orderId = node.path("orderId").asText();
            long amount = node.path("amount").asLong();
            String phone = node.path("phoneNumber").asText("");
            String name = node.path("name").asText("");
            String email = node.path("email").asText("");
            paymentService.getPaymentLink(amount, orderId, phone, name);
            paymentService.rememberCustomerEmail(orderId, email);
        } catch (RuntimeException | java.io.IOException e) {
            log.warn("Could not start payment for order event: {}", e.getMessage());
        }
    }
}
