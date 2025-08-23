package com.example.f_f.email.util;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {
    private final JavaMailSender mailSender;
    public SmtpEmailSender(JavaMailSender mailSender) { this.mailSender = mailSender; }

    @Override
    public void send(String to, String subject, String body) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}