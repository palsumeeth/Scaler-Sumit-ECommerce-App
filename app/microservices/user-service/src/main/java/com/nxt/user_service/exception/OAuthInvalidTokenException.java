package com.nxt.user_service.exception;

public class OAuthInvalidTokenException extends RuntimeException {
    public OAuthInvalidTokenException(String message) {
        super(message);
    }
}
