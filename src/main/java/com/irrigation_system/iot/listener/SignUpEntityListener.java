package com.irrigation_system.iot.listener;

import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.enumeration.SignUpStatus;
import com.irrigation_system.iot.service.EmailService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SignUpEntityListener {

    private static EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        SignUpEntityListener.emailService = emailService;
    }

    @PostPersist
    public void onSignUpCreated(SignUpEntity signUpEntity) {
        try {
            if (emailService != null && SignUpStatus.PENDING.equals(signUpEntity.getStatus())) {
                log.info("Sending verification email to: {}", signUpEntity.getEmail());
                emailService.sendVerificationEmail(
                    signUpEntity.getEmail(),
                    signUpEntity.getUsername(),
                    signUpEntity.getName(),
                    signUpEntity.getCurrentVerificationToken(),
                    signUpEntity.getExpiredVerificationTokenDate().toString()
                );
            }
        } catch (Exception e) {
            log.error("Failed to send verification email during signup", e);
        }
    }

    @PostUpdate
    public void onSignUpUpdated(SignUpEntity signUpEntity) {
        try {
            if (emailService != null && SignUpStatus.SUCCESS.equals(signUpEntity.getStatus())) {
                log.info("Sending account created email to: {}", signUpEntity.getEmail());
                emailService.sendAccountCreatedEmail(
                    signUpEntity.getEmail(),
                    signUpEntity.getUsername(),
                    signUpEntity.getName(),
                    signUpEntity.getLastModifiedAt() != null ? 
                        signUpEntity.getLastModifiedAt().toString() : 
                        signUpEntity.getCreatedAt().toString()
                );
            }
        } catch (Exception e) {
            log.error("Failed to send account created email during verification", e);
        }
    }
}
