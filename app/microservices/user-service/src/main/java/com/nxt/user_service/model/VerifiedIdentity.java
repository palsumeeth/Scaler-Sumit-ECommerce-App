package com.nxt.user_service.model;

import com.nxt.user_service.auth.AuthProvider;

public record VerifiedIdentity(
        String firstName,
        String lastName,
        String email,
        String externalId,
        AuthProvider authProvider
) {}