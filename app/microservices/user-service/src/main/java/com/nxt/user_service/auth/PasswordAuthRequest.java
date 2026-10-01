package com.nxt.user_service.auth;

public record PasswordAuthRequest (
        String email,
        String password
) implements AuthRequest {

    @Override
    public AuthProvider provider() {
        return AuthProvider.PASSWORD;
    }
}
