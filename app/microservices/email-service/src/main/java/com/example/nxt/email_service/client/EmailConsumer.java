package com.example.nxt.email_service.client;

import com.example.nxt.email_service.dto.MessageDTO;
import com.example.nxt.email_service.exception.EmailDeliveryException;
import com.example.nxt.email_service.service.EmailService;
import com.example.nxt.email_service.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Log4j2
@Component
public class EmailConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Autowired
    public EmailConsumer(ObjectMapper objectMapper, EmailService emailService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    /**
     * Listens to the specified Kafka topic and sends an email based on the message received.
     *
     * @param message Message received from the Kafka Topic
     */
    @KafkaListener(topics = Constants.EMAIL_SERVICE, groupId = Constants.SIGNUP)
    public void sendEmail(String message) throws EmailDeliveryException {
        MessageDTO messageDTO = deserialize(message);
        sendEmailToRecipient(messageDTO);
    }

    @KafkaListener(topics = Constants.ORDER_PLACED, groupId = Constants.ORDER_MAIL_GROUP)
    public void onOrderPlaced(String message) throws EmailDeliveryException {
        JsonNode event = readEvent(message);
        String to = text(event, "email");
        if (to.isBlank()) {
            log.warn("[EmailConsumer][onOrderPlaced] order.placed has no email, skipping");
            return;
        }
        String orderId = text(event, "orderId");
        MessageDTO mail = new MessageDTO();
        mail.setTo(to);
        mail.setFrom("orders@nxtlvl.com");
        mail.setSubject("Order " + orderId + " placed");
        mail.setBody("Hi " + text(event, "name")
                + ", your order " + orderId
                + " is placed. Payment method: " + text(event, "paymentMethod")
                + ". Amount: " + rupees(event));
        sendEmailToRecipient(mail);
    }

    @KafkaListener(topics = Constants.PAYMENT_CONFIRMED, groupId = Constants.PAYMENT_MAIL_GROUP)
    public void onPaymentConfirmed(String message) throws EmailDeliveryException {
        JsonNode event = readEvent(message);
        String to = text(event, "email");
        if (to.isBlank()) {
            log.warn("[EmailConsumer][onPaymentConfirmed] payment.confirmed has no email, skipping");
            return;
        }
        String orderId = text(event, "orderId");
        String receiptId = text(event, "receiptId");
        MessageDTO mail = new MessageDTO();
        mail.setTo(to);
        mail.setFrom("payments@nxtlvl.com");
        mail.setSubject("Payment receipt " + receiptId);
        mail.setBody("Payment for order " + orderId
                + " via " + text(event, "gateway")
                + " is " + text(event, "status")
                + ". Receipt " + receiptId
                + ". Amount: " + rupees(event));
        sendEmailToRecipient(mail);
    }

    private JsonNode readEvent(String message) throws EmailDeliveryException {
        try {
            return objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            log.error("[EmailConsumer][readEvent] Failed to process the message: {}", message, e);
            throw new EmailDeliveryException("Event is not valid JSON");
        }
    }

    private static String text(JsonNode event, String field) {
        return event.path(field).asText("");
    }

    private static String rupees(JsonNode event) {
        JsonNode amount = event.get("amount");
        if (amount == null || amount.isNull()) {
            return "0.00";
        }
        try {
            BigDecimal minor = amount.isNumber()
                    ? amount.decimalValue()
                    : new BigDecimal(amount.asText());
            return minor.movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException ex) {
            return amount.asText("");
        }
    }

    private MessageDTO deserialize(String message) throws EmailDeliveryException {
        try {
            return objectMapper.readValue(message, MessageDTO.class);
        } catch (JsonProcessingException e) {
            log.error("[EmailConsumer][deserializeMessage] Failed to process the message: {}", message, e);
            throw new EmailDeliveryException("MessageDTO is Null after JSON conversion");
        }
    }

    private void sendEmailToRecipient(MessageDTO messageDTO) throws EmailDeliveryException {
        Optional.ofNullable(messageDTO)
                .orElseThrow(()-> new EmailDeliveryException("MessageDTO is null after JSON conversion"));

        try {
            emailService.sendEmail(messageDTO);
            log.info("[EmailConsumer][] Email sent successfully to {}", messageDTO.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}. The attempt is already stored.", messageDTO.getTo());
        }
    }
}