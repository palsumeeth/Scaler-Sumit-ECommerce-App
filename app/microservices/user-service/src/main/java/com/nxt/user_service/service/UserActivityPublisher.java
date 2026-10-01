package com.nxt.user_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.user_service.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class UserActivityPublisher {

    static final String USER_REGISTERED_TOPIC = "user.registered";
    static final String EMAIL_TOPIC = "emailservice";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserActivityPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void userRegistered(User user) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "USER_REGISTERED");
        event.put("userId", user.getId());
        event.put("email", user.getEmail());
        send(USER_REGISTERED_TOPIC, event);

        Map<String, String> mail = new LinkedHashMap<>();
        mail.put("to", user.getEmail());
        mail.put("from", "welcome@nxtlvl.com");
        mail.put("subject", "Welcome to Nxtlvl");
        mail.put("body", "Welcome to NxtlvlThreadz. Your account is ready.");
        send(EMAIL_TOPIC, mail);
    }

    public void passwordResetRequested(String email, String resetLink) {
        Map<String, String> mail = new LinkedHashMap<>();
        mail.put("to", email);
        mail.put("from", "welcome@nxtlvl.com");
        mail.put("subject", "Reset your Nxtlvl password");
        mail.put("body", "Use this link to reset your password: " + resetLink);
        send(EMAIL_TOPIC, mail);
    }

    private void send(String topic, Object payload) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize event for topic {}", topic);
        } catch (RuntimeException e) {
            log.warn("Could not publish event to {}: {}", topic, e.getMessage());
        }
    }
}
