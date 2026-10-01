package com.nxt.order_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.order_service.entity.CustomerOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class OrderEventPublisher {

    public static final String ORDER_PLACED_TOPIC = "order.placed";
    public static final String EMAIL_TOPIC = "emailservice";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void orderPlaced(CustomerOrder order) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "ORDER_PLACED");
        event.put("orderId", String.valueOf(order.getId()));
        event.put("userId", order.getUserId());
        event.put("email", order.getEmail());
        event.put("name", order.getCustomerName());
        event.put("phoneNumber", order.getPhoneNumber());
        event.put("amount", order.getTotalAmount().movePointRight(2).longValue());
        event.put("paymentMethod", order.getPaymentMethod().name());
        send(ORDER_PLACED_TOPIC, event);
    }

    public void orderConfirmed(CustomerOrder order) {
        Map<String, String> mail = new LinkedHashMap<>();
        mail.put("to", order.getEmail());
        mail.put("from", "orders@nxtlvl.com");
        mail.put("subject", "Order " + order.getId() + " confirmed");
        mail.put("body", "Your order " + order.getId()
                + " is " + order.getStatus()
                + ". Receipt: " + order.getReceiptId()
                + ". Total: " + order.getTotalAmount());
        send(EMAIL_TOPIC, mail);
    }

    private void send(String topic, Object payload) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("Could not publish to {}: {}", topic, e.getMessage());
        }
    }
}
