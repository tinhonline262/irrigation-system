package com.irrigation_system.iot.service;


import com.irrigation_system.iot.entity.UserEntity;

import java.time.Duration;

public interface JwtKeyService {

    String generateToken(UserEntity user, Duration expiry);

    String generateRefreshToken(UserEntity user, Duration expiry);

    boolean validateToken(String token);

    String extractUsername(String token);
}
