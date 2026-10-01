package com.nxt.user_service.auth;

public record GoogleAuthRequest(String idToken) implements AuthRequest {

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }
}