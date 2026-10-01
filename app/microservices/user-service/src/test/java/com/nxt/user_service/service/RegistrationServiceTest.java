package com.nxt.user_service.service;

import com.nxt.user_service.dto.CreateUserReqDTO;
import com.nxt.user_service.dto.CreateUserRespDTO;
import com.nxt.user_service.entity.RefreshToken;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.exception.UserAlreadyExistsException;
import com.nxt.user_service.model.ResponseStatus;
import com.nxt.user_service.model.TokenPair;
import com.nxt.user_service.repo.RefreshTokenRepository;
import com.nxt.user_service.repo.UserRepository;
import com.nxt.user_service.service.token.JwtTokenService;
import com.nxt.user_service.service.token.RefreshTokenFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenFactory refreshTokenFactory;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserActivityPublisher userActivityPublisher;

    @InjectMocks
    private RegistrationService registrationService;

    private CreateUserReqDTO req;
    @BeforeEach
    void setup() {
        req = new CreateUserReqDTO();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("StrongPassword@123");
        req.setRegistrationType("EMAIL");
    }
    // email does not exist
    // user is saved
    // password is encoded
    // tokens are generated
    // refresh token is persisted
    // resp contains expected data
    @Test
    void shouldCreateUserAndGenerateTokensWhenEmailDoesNotExist() {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("john@example.com");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        var tokens = new TokenPair("access", "refresh");
        when(jwtTokenService.generateTokens(savedUser))
                .thenReturn(tokens);

        when(refreshTokenFactory.fromRaw("refresh", 1L))
                .thenReturn(new RefreshToken());

        CreateUserRespDTO resp = registrationService.createUser(req);

        assertThat(resp.getUserId()).isEqualTo(1L);
        assertThat(resp.getEmail()).isEqualTo("john@example.com");
        assertThat(resp.getAccessToken()).isEqualTo("access");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh");
        assertThat(resp.getResponseStatus()).isEqualTo(ResponseStatus.SUCCESSFUL);

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(passwordEncoder).encode(anyString());
        verify(jwtTokenService).generateTokens(savedUser);
    }

    // negative : email already exists
    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> registrationService.createUser(req));

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).save(any());
    }
}