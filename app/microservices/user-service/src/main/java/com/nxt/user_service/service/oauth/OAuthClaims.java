package com.nxt.user_service.service.oauth;

public record OAuthClaims(
        String subject,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified
) {}
