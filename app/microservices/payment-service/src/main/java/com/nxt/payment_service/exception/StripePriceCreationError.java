package com.nxt.payment_service.exception;

public class StripePriceCreationError extends RuntimeException {

    public StripePriceCreationError(String message, Throwable cause) {
        super(message, cause);
    }
}
