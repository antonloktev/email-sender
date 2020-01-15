package com.netcracker.edu.sender;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.util.List;
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

    private void saveToDB(Person p) {
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String username = "parser";
        String password = "1234";
        try (Connection conn = DriverManager.getConnection(url, username, password)){
            PreparedStatement stmnt;

            // update time, if the user is already in the DB
            String sql = "MERGE INTO EMAILS\n" +
                    "USING DUAL ON ( name = ? AND email = ?)\n" +
                    "WHEN MATCHED THEN UPDATE SET date_value = sysdate\n" +
                    "WHEN NOT MATCHED THEN INSERT VALUES (EMAILS_ID_INCREMENT.nextval,?,?,sysdate)\n";

            stmnt = conn.prepareStatement(sql);
            // in case of updating
            stmnt.setString(1, p.getName());
            stmnt.setString(2, p.getEmail());
            // in case of inserting
            stmnt.setString(3, p.getName());
            stmnt.setString(4, p.getEmail());

        stmnt.executeUpdate();

        } catch (SQLException e) {
            log.error("Error while saving user " + p, e);
        }

    }

    public void sendMessage(List<Person> persons) {
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, PASSWORD);
            }
        });

        for (Person p : persons) {
            StringBuilder msg = new StringBuilder("Hello, ! It's a test message, don't respond. Have a nice day!");
            int placeForName = 7;
            msg.insert(placeForName, p.getName()); // add name between comma and exclamation mark from file
            try {
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(EMAIL_FROM));
                mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(p.getEmail()));
                mimeMessage.setSubject(SUBJECT);
                mimeMessage.setText(String.valueOf(msg));

                Transport.send(mimeMessage);
                log.info("Message to address -> " + p.getEmail() + " <- successfully sent");

                saveToDB(p);
                log.info("User " + p + " saved");

            } catch (MessagingException e) {
                log.error("Message sending failed");
            }
        }
    }
}
