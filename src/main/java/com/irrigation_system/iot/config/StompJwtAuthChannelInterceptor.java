package com.irrigation_system.iot.config;

import com.irrigation_system.iot.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;

@Slf4j
public class StompJwtAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final TokenBlacklistService tokenBlacklistService;

    public StompJwtAuthChannelInterceptor(JwtDecoder jwtDecoder,
                                         JwtAuthenticationConverter jwtAuthenticationConverter,
                                         TokenBlacklistService tokenBlacklistService) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand()) || SimpMessageType.CONNECT.equals(accessor.getMessageType())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders == null || authHeaders.isEmpty()) {
                log.warn("WebSocket CONNECT rejected: missing Authorization header");
                throw new MessageDeliveryException("Missing Authorization header for WebSocket CONNECT");
            }

            String authorization = authHeaders.get(0);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT rejected: invalid Authorization header format");
                throw new MessageDeliveryException("Invalid Authorization header for WebSocket CONNECT");
            }

            String token = authorization.substring(7);
            if (tokenBlacklistService.isAccessTokenBlacklisted(token)) {
                log.warn("WebSocket CONNECT rejected: token is blacklisted");
                throw new MessageDeliveryException("WebSocket JWT token has been blacklisted");
            }

            try {
                Jwt jwt = jwtDecoder.decode(token);
                Authentication authentication = jwtAuthenticationConverter.convert(jwt);
                if (authentication == null) {
                    log.warn("WebSocket CONNECT rejected: could not convert JWT to Authentication");
                    throw new MessageDeliveryException("Unable to authenticate WebSocket JWT token");
                }
                accessor.setUser(authentication);
                message = MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                log.debug("WebSocket CONNECT authenticated: user={}", authentication.getName());
            } catch (JwtException ex) {
                log.warn("WebSocket CONNECT rejected: invalid JWT — {}", ex.getMessage());
                throw new MessageDeliveryException(message,
                        "Invalid or expired WebSocket JWT token: " + ex.getMessage());
            }
        }

        return message;
    }
}
