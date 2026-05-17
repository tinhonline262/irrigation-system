package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendVerificationEmail(String email, String username, String fullName, String verifyToken, String expiredDate) {
        try {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("username", username);
            context.setVariable("verifyToken", verifyToken);
            context.setVariable("expiredDate", expiredDate);

            String htmlContent = templateEngine.process("email/verify-user-mail-template", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Email Verification - Irrigation System");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@irrigation-system.com");

            javaMailSender.send(message);
            log.info("Verification email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendAccountCreatedEmail(String email, String username, String fullName, String createdAt) {
        try {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("username", username);
            context.setVariable("email", email);
            context.setVariable("createdAt", createdAt);

            String htmlContent = templateEngine.process("email/complete-user-mail-template", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Account Created Successfully - Irrigation System");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@irrigation-system.com");

            javaMailSender.send(message);
            log.info("Account created email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send account created email to: {}", email, e);
            throw new RuntimeException("Failed to send account created email", e);
        }
    }
}
