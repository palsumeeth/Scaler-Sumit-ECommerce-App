package com.nxt.cart_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.cart_service.dto.CartResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class CartCache {

    static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CartCache(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public Optional<CartResponse> get(Long userId) {
        try {
            String json = redis.opsForValue().get(key(userId));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, CartResponse.class));
        } catch (Exception e) {
            log.warn("Redis read failed for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(CartResponse cart) {
        try {
            redis.opsForValue().set(key(cart.getUserId()), objectMapper.writeValueAsString(cart), TTL);
        } catch (Exception e) {
            log.warn("Redis write failed for user {}: {}", cart.getUserId(), e.getMessage());
        }
    }

    private static String key(Long userId) {
        return "cart:user:" + userId;
    }
}
