package com.nxt.payment_service.exception;

public class StripePaymentCreationError extends RuntimeException{
    public StripePaymentCreationError(String message, Throwable cause) {
        super(message, cause);
    }
}