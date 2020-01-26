package com.netcracker.edu.sender;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class EmailSender implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final String EMAIL_FROM = "xxx@gmail.com";  // change accordingly
    private final String PASSWORD = "admin";  //change accordingly
    private final String SUBJECT = "Greeting! \uD83D\uDE03";

    private final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private final String ORACLE_USER = "parser";
    private final String ORACLE_PASS = "1234";

    // multithreading parameters
    private static final int NUMBER_OF_THREADS = 4;
    private static final int NUMBER_OF_RECORDS_FOR_THREAD = 2;

    private Properties props;

    private List<Person> persons;

    public EmailSender() {
        props = new Properties();

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
    }

    public EmailSender(List<Person> partOfPersons) {
        this();
        this.persons = partOfPersons;
    }

    @Override
    public void run() {
        sendMessage(persons);
    }

    private void saveToDB(Person p) {
        try (Connection conn = DriverManager.getConnection(ORACLE_URL, ORACLE_USER, ORACLE_PASS)){
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

    public List<Person> getAll(){
        String sql = "select * from emails";

        List persons = new ArrayList<Person>();

        try {
            PreparedStatement stmnt;
            stmnt = DriverManager.getConnection(ORACLE_URL, ORACLE_USER, ORACLE_PASS).prepareStatement(sql);
            ResultSet rs = stmnt.executeQuery();
            while(rs.next()) {
                Person p = new Person();
                p.setName(rs.getString("name"));
                p.setEmail(rs.getString("email"));
                p.setLastMessageDate(rs.getTimestamp("date_value"));
                persons.add(p);
            }
        } catch (SQLException e) {
            log.error("Error during select query", e);
        }
        return persons;
    }

    // calculating time from DB plus timeout
    public Timestamp getEndOfTimeout(Person p, int timeout) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(p.getLastMessageDate().getTime());
        cal.add(Calendar.SECOND, timeout);
        return new Timestamp(cal.getTime().getTime());
    }

    public Timestamp getCurrentTime() {
        Date date = new Date();
        long time = date.getTime();
        return new Timestamp(time);
    }

    // send message to all users from DB considering timeout
    public void sendToAllFromDB(int timeout) {
        List<Person> persons = getAll();
        List<Person> filtered = new ArrayList<>();
        for (Person p : persons) {
            Timestamp requiredTime = getEndOfTimeout(p, timeout);
            Timestamp currentTime = getCurrentTime();
            // last time of message plus timeout less than current time, so we can send message again
            if (requiredTime.before(currentTime)) {
                filtered.add(p);
            } else {
                log.info("Timeout for " + p.getEmail() + " is not passed");
            }
        }
        sendMessage(filtered);
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

    // split the list of persons so that each part consists of a given number of records (except maybe the last)
    public static <Person> List<List<Person>> splitList(List<Person> persons, int quantityInPart) {
        List<List<Person>> parts = new ArrayList<>();

        for (int i = 0; i < persons.size(); i += quantityInPart) {
            parts.add(new ArrayList<>(
                    persons.subList(i, Math.min(persons.size(), i + quantityInPart)))
            );
        }
        return parts;
    }

    public void sendUsingMultithreading(List<Person> persons) {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        log.info("Multithreading sending started. Number of threads: " + NUMBER_OF_THREADS);

        List<List<Person>> parts = splitList(persons, NUMBER_OF_RECORDS_FOR_THREAD);

        for(List<Person> part : parts) {
            executor.execute(new EmailSender(part));
        }

        executor.shutdown();
    }
}
