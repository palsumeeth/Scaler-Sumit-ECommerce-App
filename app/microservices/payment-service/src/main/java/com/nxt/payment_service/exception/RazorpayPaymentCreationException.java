package com.nxt.payment_service.exception;

public class RazorpayPaymentCreationException extends RuntimeException {

    public RazorpayPaymentCreationException() { super("Something went wrong while processing the payment."); }

    public RazorpayPaymentCreationException(String message) { super(message); }

    public RazorpayPaymentCreationException(String message, Throwable cause) { super(message, cause); }
}