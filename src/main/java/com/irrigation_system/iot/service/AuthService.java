package com.irrigation_system.iot.service;


import com.irrigation_system.iot.dto.LoginRequestDTO;
import com.irrigation_system.iot.dto.LoginResponseDTO;
import com.irrigation_system.iot.dto.RegistrationDTO;
import com.irrigation_system.iot.dto.TokenResponseDTO;
import com.irrigation_system.iot.entity.UserEntity;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    void logout(String accessToken, String refreshToken);

    TokenResponseDTO refreshToken(String refreshToken);

    LoginResponseDTO getLoginResponseWithAssignedTokens(UserEntity userEntity);

    void signup(RegistrationDTO registrationDTO);

    void verifyUserRegistration(String token);

    void refreshUserVerification(String username);
}
