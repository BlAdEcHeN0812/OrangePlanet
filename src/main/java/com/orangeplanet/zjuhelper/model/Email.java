package com.orangeplanet.zjuhelper.model;

public class Email {
    private String sender;
    private String subject;
    private String date;
    private String size;

    public Email() {
    }

    public Email(String sender, String subject, String date, String size) {
        this.sender = sender;
        this.subject = subject;
        this.date = date;
        this.size = size;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
