    package com.irrigation_system.iot.dto;

    import lombok.Builder;
    import lombok.Data;

    @Data
    @Builder
    public class TokenResponseDTO {
        private String accessToken;
        private String refreshToken;
    }
