package com.irrigation_system.iot.service;

public interface TokenBlacklistService {
    void blacklistAccessToken(String token);
    void blacklistRefreshToken(String refreshToken);
    boolean isAccessTokenBlacklisted(String token);
    boolean isRefreshTokenBlacklisted(String token);
}
