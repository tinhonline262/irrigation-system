package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.LoginRequestDTO;
import com.irrigation_system.iot.dto.LoginResponseDTO;
import com.irrigation_system.iot.dto.RegistrationDTO;
import com.irrigation_system.iot.dto.TokenResponseDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.enumeration.SignUpStatus;
import com.irrigation_system.iot.exception.BlacklistedTokenException;
import com.irrigation_system.iot.exception.InvalidTokenException;
import com.irrigation_system.iot.exception.LoginNotValidException;
import com.irrigation_system.iot.exception.SignUpNotValidException;
import com.irrigation_system.iot.mapper.AuthMapper;
import com.irrigation_system.iot.properties.JwtProperties;
import com.irrigation_system.iot.repository.SignUpRepository;
import com.irrigation_system.iot.service.JwtKeyService;
import com.irrigation_system.iot.service.TokenBlacklistService;
import com.irrigation_system.iot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthMapper authMapper;

    @Mock
    private SignUpRepository signUpRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtKeyService jwtKeyService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getTokenExp()).thenReturn(java.time.Duration.ofSeconds(3600));
        lenient().when(jwtProperties.getRefreshTokenExp()).thenReturn(java.time.Duration.ofSeconds(86400));
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        UserEntity user = new UserEntity();
        user.setUsername("tester");
        user.setPassword("encoded-password");

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("tester");
        request.setPassword("secret");

        LoginResponseDTO responseDto = new LoginResponseDTO();
        responseDto.setUsername("tester");

        when(userService.getUserByUsername("tester")).thenReturn(user);
        when(passwordEncoder.matches("secret", "encoded-password")).thenReturn(true);
        when(authMapper.map(user)).thenReturn(responseDto);
        when(jwtKeyService.generateToken(user, java.time.Duration.ofSeconds(3600))).thenReturn("access-token");
        when(jwtKeyService.generateRefreshToken(user, java.time.Duration.ofSeconds(86400))).thenReturn("refresh-token");

        LoginResponseDTO result = authService.login(request);

        assertThat(result.getToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUsername()).isEqualTo("tester");
    }

    @Test
    void login_withInvalidPassword_throwsLoginNotValidException() {
        UserEntity user = new UserEntity();
        user.setPassword("encoded-password");

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("tester");
        request.setPassword("badpass");

        when(userService.getUserByUsername("tester")).thenReturn(user);
        when(passwordEncoder.matches("badpass", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginNotValidException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void logout_withValidTokens_blacklistsBothTokens() {
        authService.logout("access-token", "refresh-token");

        verify(tokenBlacklistService).blacklistAccessToken("access-token");
        verify(tokenBlacklistService).blacklistRefreshToken("refresh-token");
    }

    @Test
    void refreshToken_withValidToken_returnsNewTokensAndBlacklistsOldOne() {
        UserEntity user = new UserEntity();
        user.setUsername("tester");

        when(jwtKeyService.validateToken("valid-refresh-token")).thenReturn(true);
        when(tokenBlacklistService.isRefreshTokenBlacklisted("valid-refresh-token")).thenReturn(false);
        when(jwtKeyService.extractUsername("valid-refresh-token")).thenReturn("tester");
        when(userService.getUserByUsername("tester")).thenReturn(user);
        when(jwtKeyService.generateToken(user, java.time.Duration.ofSeconds(3600))).thenReturn("new-access");
        when(jwtKeyService.generateRefreshToken(user, java.time.Duration.ofSeconds(86400))).thenReturn("new-refresh");

        TokenResponseDTO result = authService.refreshToken("valid-refresh-token");

        assertThat(result.getAccessToken()).isEqualTo("new-access");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
        verify(tokenBlacklistService).blacklistRefreshToken("valid-refresh-token");
    }

    @Test
    void refreshToken_whenTokenBlacklisted_throwsBlacklistedTokenException() {
        when(jwtKeyService.validateToken("blacklisted-token")).thenReturn(true);
        when(tokenBlacklistService.isRefreshTokenBlacklisted("blacklisted-token")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken("blacklisted-token"))
                .isInstanceOf(BlacklistedTokenException.class)
                .hasMessageContaining("Refresh token is blacklisted");
    }

    @Test
    void refreshToken_whenTokenInvalid_throwsInvalidTokenException() {
        when(jwtKeyService.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void signup_withNewUsername_persistsPendingSignUpAndAssignsToken() {
        RegistrationDTO registration = new RegistrationDTO();
        registration.setUsername("tester");
        registration.setEmail("tester@example.com");
        registration.setPassword("secret");

        SignUpEntity signUpEntity = new SignUpEntity();
        signUpEntity.setUsername("tester");
        signUpEntity.setEmail("tester@example.com");

        when(signUpRepository.existsByUsernameAndStatusIn(eq("tester"), anyList())).thenReturn(false);
        when(signUpRepository.existsByEmailAndStatusIn(eq("tester@example.com"), anyList())).thenReturn(false);
        when(authMapper.map(any(RegistrationDTO.class), any(SignUpEntity.class))).thenReturn(signUpEntity);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(signUpRepository.save(any(SignUpEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.signup(registration);

        ArgumentCaptor<SignUpEntity> captor = ArgumentCaptor.forClass(SignUpEntity.class);
        verify(signUpRepository).save(captor.capture());
        SignUpEntity saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(SignUpStatus.PENDING);
        assertThat(saved.getPassword()).isEqualTo("encoded-secret");
        assertThat(saved.getCurrentVerificationToken()).isNotBlank();
    }

    @Test
    void signup_whenUsernameAlreadyExists_throwsSignUpNotValidException() {
        RegistrationDTO registration = new RegistrationDTO();
        registration.setUsername("tester");
        registration.setEmail("tester@example.com");

        when(signUpRepository.existsByUsernameAndStatusIn(eq("tester"), anyList())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(registration))
                .isInstanceOf(SignUpNotValidException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void signup_whenEmailAlreadyRegistered_throwsSignUpNotValidException() {
        RegistrationDTO registration = new RegistrationDTO();
        registration.setUsername("tester");
        registration.setEmail("tester@example.com");

        when(signUpRepository.existsByUsernameAndStatusIn(eq("tester"), anyList())).thenReturn(false);
        when(signUpRepository.existsByEmailAndStatusIn(eq("tester@example.com"), anyList())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(registration))
                .isInstanceOf(SignUpNotValidException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void verifyUserRegistration_whenTokenValid_setsStatusSuccessAndCreatesUser() {
        SignUpEntity signUpEntity = new SignUpEntity();
        signUpEntity.setUsername("tester");
        signUpEntity.setStatus(SignUpStatus.PENDING);
        signUpEntity.setCurrentVerificationToken("token");
        signUpEntity.setExpiredVerificationTokenDate(LocalDateTime.now().plusMinutes(10));

        when(signUpRepository.findByCurrentVerificationTokenAndStatusIn(eq("token"), anyList()))
                .thenReturn(Optional.of(signUpEntity));
        when(signUpRepository.save(any(SignUpEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.verifyUserRegistration("token");

        assertThat(signUpEntity.getStatus()).isEqualTo(SignUpStatus.SUCCESS);
        verify(userService).createUser(signUpEntity);
    }

    @Test
    void verifyUserRegistration_whenTokenExpired_throwsSignUpNotValidException() {
        SignUpEntity signUpEntity = new SignUpEntity();
        signUpEntity.setStatus(SignUpStatus.PENDING);
        signUpEntity.setCurrentVerificationToken("token");
        signUpEntity.setExpiredVerificationTokenDate(LocalDateTime.now().minusMinutes(1));

        when(signUpRepository.findByCurrentVerificationTokenAndStatusIn(eq("token"), anyList()))
                .thenReturn(Optional.of(signUpEntity));

        assertThatThrownBy(() -> authService.verifyUserRegistration("token"))
                .isInstanceOf(SignUpNotValidException.class)
                .hasMessageContaining("Token is expired");
    }
}
