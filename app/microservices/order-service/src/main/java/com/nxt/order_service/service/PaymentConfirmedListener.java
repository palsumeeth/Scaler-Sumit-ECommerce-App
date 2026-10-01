package com.nxt.order_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentConfirmedListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public PaymentConfirmedListener(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment.confirmed", groupId = "order-service")
    public void onPaymentConfirmed(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            long orderId = Long.parseLong(node.path("orderId").asText());
            String receiptId = node.path("receiptId").asText(null);
            orderService.markPaid(orderId, receiptId);
        } catch (RuntimeException | java.io.IOException e) {
            log.warn("Could not apply payment confirmation: {}", e.getMessage());
        }
    }
}
