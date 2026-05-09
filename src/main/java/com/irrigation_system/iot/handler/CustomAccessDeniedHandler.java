package com.irrigation_system.iot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irrigation_system.iot.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.error("User: '{}' attempted to access the protected URL: '{}' with authorities: {}. Exception: {}", 
                    auth.getName(), request.getRequestURI(), auth.getAuthorities(), accessDeniedException.getMessage());
        } else {
            log.error("Access denied for URL: {}. Exception: {}", request.getRequestURI(), accessDeniedException.getMessage());
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("You don't have permission to access this resource")
                .path(request.getRequestURI())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}