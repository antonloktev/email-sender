package com.netcracker.edu.sender;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class EmailSender {
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final String EMAIL_FROM = "xxx@gmail.com";  // change accordingly
    private final String PASSWORD = "admin";  //change accordingly
    private final String SUBJECT = "Greeting! \uD83D\uDE03";

    private Properties props;

    public EmailSender() {
        props = new Properties();

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
    }

    public void sendMessage(String email, String message) {
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, PASSWORD);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(EMAIL_FROM));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            mimeMessage.setSubject(SUBJECT);
            mimeMessage.setText(message);

            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Message sending failed");
        }
    }
}
