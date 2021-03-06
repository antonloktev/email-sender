package com.netcracker.edu.sender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(Application.class, args);
        EmailSender e = new EmailSender();
        e.sendUsingMultithreading(Parser.readFromExcel("emails.xlsx"));
    }
}


