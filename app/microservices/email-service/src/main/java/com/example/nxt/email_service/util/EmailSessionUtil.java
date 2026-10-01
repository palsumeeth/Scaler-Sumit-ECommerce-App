package com.example.nxt.email_service.util;

import org.springframework.stereotype.Component;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

import static com.example.nxt.email_service.util.Constants.*;
import static com.example.nxt.email_service.util.Constants.SMTP_STARTTLS_ENABLE;

@Component
public class EmailSessionUtil {

    public static Session createGmailSession(String email, String password) {
        Properties props = new Properties();
        props.put(MAIL_SMTP_HOST, SMTP_HOST);
        props.put(MAIL_SMTP_AUTH, SMTP_AUTH);
        props.put(MAIL_SMTP_PORT, SMTP_PORT);
        props.put(MAIL_SMTP_STARTTLS, SMTP_STARTTLS_ENABLE);

        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        };

        return Session.getInstance(props, authenticator);
    }
}
