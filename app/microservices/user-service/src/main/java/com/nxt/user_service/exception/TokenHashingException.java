package com.nxt.user_service.exception;

public class TokenHashingException extends RuntimeException {
    public TokenHashingException(String message, Throwable cause) {
        super(message, cause);
    }
}
