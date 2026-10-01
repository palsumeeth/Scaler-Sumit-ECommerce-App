package com.nxt.user_service.exception;

public class KeyResourceAccessException extends RuntimeException {
    public KeyResourceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}