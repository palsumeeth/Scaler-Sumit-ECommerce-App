package com.example.nxt.email_service.util;


public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SIGNUP = "signup";
    public static final String EMAIL_SERVICE = "emailservice";
    public static final String ORDER_PLACED = "order.placed";
    public static final String PAYMENT_CONFIRMED = "payment.confirmed";
    public static final String ORDER_MAIL_GROUP = "email-service-orders";
    public static final String PAYMENT_MAIL_GROUP = "email-service-payments";

    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";

    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_PORT = "587";
    public static final String SMTP_STARTTLS_ENABLE = "true";
}