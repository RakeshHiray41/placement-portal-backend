package com.placement.placement.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setTo(to);
        helper.setSubject(subject);
        // FIX: second argument tells JavaMail whether the body is HTML.
        // It was "false" even though the body contains real <p> tags,
        // so Gmail/Outlook showed the literal <p>...</p> text instead of
        // rendering it as formatted paragraphs. Must be "true".
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}