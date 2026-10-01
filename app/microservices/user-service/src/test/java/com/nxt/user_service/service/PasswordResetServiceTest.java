package com.nxt.user_service.service;

import com.nxt.user_service.dto.PasswordResetConfirmDTO;
import com.nxt.user_service.dto.PasswordResetReqDTO;
import com.nxt.user_service.entity.PasswordResetToken;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.exception.InvalidTokenException;
import com.nxt.user_service.exception.TokenExpiredException;
import com.nxt.user_service.exception.TokenMissingException;
import com.nxt.user_service.repo.PasswordResetRepository;
import com.nxt.user_service.repo.UserRepository;
import com.nxt.user_service.service.email.EmailSender;
import com.nxt.user_service.service.token.TokenHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private TokenHashService tokenHashService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;
    @BeforeEach
    void setup() {
        user = new User();
        user.setId(10L);
        user.setEmail("john@example.com");
    }

    @Nested
    class RequestReset {

        @Test
        void shouldGenerateAndPersistResetTokenWhenUserExists() {
            PasswordResetReqDTO req = new PasswordResetReqDTO();
            req.setEmail("john@example.com");

            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(user));

            when(tokenHashService.hashToken(anyString()))
                    .thenReturn("hashed-token");

            passwordResetService.requestReset(req);

            verify(passwordResetRepository).save(any(PasswordResetToken.class));
            verify(emailSender).sendPasswordReset(eq("john@example.com"), contains("token="));
        }

        @Test
        void shouldSilentlyDoNothingWhenUserDoesNotExist() {
            PasswordResetReqDTO req = new PasswordResetReqDTO();
            req.setEmail("ghost@example.com");

            passwordResetService.requestReset(req);

            verify(passwordResetRepository, never()).save(any());
            verify(emailSender, never()).sendPasswordReset(any(), any());
        }
    }

    @Nested
    class ConfirmReset {

        @Test
        void shouldThrowWhenTokenIsMissing() {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setToken(" ");
            req.setNewPassword("new-secret");

            assertThrows(TokenMissingException.class,
                    () -> passwordResetService.confirmReset(req));
        }

        @Test
        void shouldThrowWhenTokenIsInvalid() {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setToken("bad-token");
            req.setNewPassword("new-secret");

            when(tokenHashService.hashToken("bad-token"))
                    .thenReturn("hashed-bad");

            when(passwordResetRepository.findByTokenHash("hashed-bad"))
                    .thenReturn(Optional.empty());

            assertThrows(InvalidTokenException.class,
                    () -> passwordResetService.confirmReset(req));
        }

        @Test
        void shouldThrowWhenTokenIsExpiredOrUsed() {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setToken("raw-token");
            req.setNewPassword("new-secret");

            PasswordResetToken tokenEntity = PasswordResetToken.from(
                    "hashed-token", user.getId(), Instant.now().minusSeconds(10)
            );

            when(tokenHashService.hashToken("raw-token"))
                    .thenReturn("hashed-token");

            when(passwordResetRepository.findByTokenHash("hashed-token"))
                    .thenReturn(Optional.of(tokenEntity));

            assertThrows(TokenExpiredException.class,
                    () -> passwordResetService.confirmReset(req));
        }
    }
}