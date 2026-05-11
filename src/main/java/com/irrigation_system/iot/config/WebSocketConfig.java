package com.irrigation_system.iot.config;

import com.irrigation_system.iot.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final TokenBlacklistService tokenBlacklistService;

    // Pre-initialize the scheduler as a field so it is ready when
    // configureMessageBroker() is called during Spring context wiring
    // (before @Bean lifecycle methods are fully processed).
    private final ThreadPoolTaskScheduler heartbeatScheduler = createHeartbeatScheduler();

    private static ThreadPoolTaskScheduler createHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // setAllowedOriginPatterns required for SockJS in Spring 6 / Spring Boot 3
                .setAllowedOriginPatterns("http://localhost:5173")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(heartbeatScheduler); // use pre-initialized scheduler
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtAuthChannelInterceptor());
    }

    @Bean
    public ChannelInterceptor stompJwtAuthChannelInterceptor() {
        return new StompJwtAuthChannelInterceptor(jwtDecoder, jwtAuthenticationConverter, tokenBlacklistService);
    }

    @Bean(name = "messageBrokerTaskScheduler")
    public ThreadPoolTaskScheduler messageBrokerTaskScheduler() {
        // Expose the pre-initialized scheduler as a Spring bean so other
        // components (e.g. SockJS frame relay) can also use it if needed.
        return heartbeatScheduler;
    }
}
