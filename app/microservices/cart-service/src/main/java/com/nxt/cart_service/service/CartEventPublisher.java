package com.nxt.cart_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.cart_service.entity.Cart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class CartEventPublisher {

    static final String TOPIC = "cart.updated";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CartEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void cartUpdated(Cart cart) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "CART_UPDATED");
        event.put("userId", cart.getUserId());
        event.put("cartId", cart.getId());
        event.put("itemCount", cart.getItems().size());
        try {
            kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("Could not publish cart update: {}", e.getMessage());
        }
    }
}
