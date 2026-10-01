package com.nxt.user_service.service.email;

import com.nxt.user_service.service.UserActivityPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailSender {

    private final UserActivityPublisher userActivityPublisher;

    public EmailSender(UserActivityPublisher userActivityPublisher) {
        this.userActivityPublisher = userActivityPublisher;
    }

    public void sendPasswordReset(String email, String resetLink) {
        log.info("Sending request for Password Reset Link {} for User with Email {} ", resetLink, email);
        userActivityPublisher.passwordResetRequested(email, resetLink);
    }
}
