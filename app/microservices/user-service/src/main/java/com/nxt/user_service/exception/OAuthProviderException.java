package com.nxt.user_service.exception;

public class OAuthProviderException extends RuntimeException {
    public OAuthProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}