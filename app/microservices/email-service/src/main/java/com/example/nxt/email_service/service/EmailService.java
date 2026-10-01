package com.example.nxt.email_service.service;

import com.example.nxt.email_service.dto.MessageDTO;
import com.example.nxt.email_service.entity.NotificationLog;
import com.example.nxt.email_service.exception.EmailDeliveryException;
import com.example.nxt.email_service.repo.NotificationLogRepository;
import com.example.nxt.email_service.util.EmailSessionUtil;
import com.example.nxt.email_service.util.EmailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import java.time.Instant;

@Service
public class EmailService {

    private final NotificationLogRepository notificationLogRepository;
    private final String mailPassword;

    public EmailService(NotificationLogRepository notificationLogRepository,
                        @Value("${nxt.mail.password}") String mailPassword) {
        this.notificationLogRepository = notificationLogRepository;
        this.mailPassword = mailPassword;
    }

    /**
     * Sends an email using the provided {@link MessageDTO} object and stores the attempt.
     *
     * @param messageDTO the {@link MessageDTO} object containing details such as sender's email, recipient's email,
     *                   subject and body.
     * @throws EmailDeliveryException if there is an error while sending the email.
     */
    public void sendEmail(MessageDTO messageDTO) throws EmailDeliveryException {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setRecipient(messageDTO.getTo());
        notificationLog.setSubject(messageDTO.getSubject());
        notificationLog.setBody(messageDTO.getBody());
        notificationLog.setCreatedAt(Instant.now());

        try {
            Session session = EmailSessionUtil.createGmailSession(messageDTO.getFrom(), mailPassword);
            EmailUtil.sendEmail(session, messageDTO.getTo(), messageDTO.getSubject(), messageDTO.getBody());
            notificationLog.setStatus("SENT");
            notificationLogRepository.save(notificationLog);
        } catch (Exception e) {
            notificationLog.setStatus("FAILED");
            notificationLog.setErrorMessage(e.getMessage());
            notificationLogRepository.save(notificationLog);
            if (e instanceof EmailDeliveryException deliveryException) {
                throw deliveryException;
            }
            throw new EmailDeliveryException("Failed to send email", e);
        }
    }
}
