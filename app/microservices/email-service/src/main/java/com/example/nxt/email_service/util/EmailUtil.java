package com.example.nxt.email_service.util;

import com.example.nxt.email_service.exception.EmailDeliveryException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@Log4j2
public class EmailUtil {

    /**
     * Sends an email using the specified session, recipient's email address, subject and body.
     *
     * @param session        the {@link Session} object that holds the email session information.
     * @param recipientEmail the email address of the recipient.
     * @param subject        the subject of the email.
     * @param body           the body content of the email.
     * @throws EmailDeliveryException if there is an error while sending the email.
     */
    public static void sendEmail(Session session,
                                 String recipientEmail,
                                 String subject,
                                 String body) throws EmailDeliveryException {
        try {
            MimeMessage message = new MimeMessage(session);

            message.addHeader(HttpHeaders.CONTENT_TYPE, "text/HTML; charset=UTF-8");
            message.addHeader("format", "flowed");
            message.addHeader("Content-Transfer-Encoding", "8bit");
            message.setFrom(new InternetAddress("some.email@gmail.com", "NoReply-JD"));
            message.setReplyTo(InternetAddress.parse(recipientEmail));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");
            message.setSentDate(new Date());
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

            log.info("[EmailUtil][sendEmail] Sending email to {}", recipientEmail);
            Transport.send(message);
            log.info("[EmailUtil][sendEmail] Email sent successfully");
        } catch (Exception e) {
            log.error("[EmailUtil][sendEmail] Email sent failed");
            throw new EmailDeliveryException("Sending email failed", e);
        }
    }
}