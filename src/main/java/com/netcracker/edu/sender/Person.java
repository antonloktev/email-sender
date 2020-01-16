package com.netcracker.edu.sender;

import java.util.Date;

public class Person {
    private String name;
    private String email;
    private Date lastMessageDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    @Override
    public String toString() {
        return name + ":" + email;
    }
}
