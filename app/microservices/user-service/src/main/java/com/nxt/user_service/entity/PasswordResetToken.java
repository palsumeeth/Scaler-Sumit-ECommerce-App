package com.nxt.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String tokenHash;
    private Instant expiresAt;
    private boolean used;

    public static PasswordResetToken from(String tokenHash, Long userId, Instant expiresAt) {
        PasswordResetToken t = new PasswordResetToken();
        t.setUserId(userId);
        t.setTokenHash(tokenHash);
        t.setExpiresAt(expiresAt);
        t.setUsed(false);
        return t;
    }
}

