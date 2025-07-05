package com.dep.soms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServices {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send a simple email asynchronously
     */
    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {} with subject: {}", to, subject, e);
        }
    }
}
