package com.example.f_f.email.util;

public interface EmailSender {
    void send(String to, String subject, String body);
}