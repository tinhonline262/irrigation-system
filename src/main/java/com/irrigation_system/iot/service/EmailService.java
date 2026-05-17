package com.irrigation_system.iot.service;

public interface EmailService {
    
    void sendVerificationEmail(String email, String username, String fullName, String verifyToken, String expiredDate);
    
    void sendAccountCreatedEmail(String email, String username, String fullName, String createdAt);
}
