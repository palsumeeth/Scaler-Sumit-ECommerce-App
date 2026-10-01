package com.nxt.user_service.exception;

public class OAuthMalformedTokenException extends RuntimeException {
    public OAuthMalformedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}