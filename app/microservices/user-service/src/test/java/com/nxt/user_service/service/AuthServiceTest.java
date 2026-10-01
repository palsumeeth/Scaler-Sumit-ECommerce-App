package com.nxt.user_service.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.nxt.user_service.auth.*;
import com.nxt.user_service.dto.LoginRespDTO;
import com.nxt.user_service.entity.RefreshToken;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.exception.UnsupportedAuthProviderException;
import com.nxt.user_service.exception.UserNotFoundException;
import com.nxt.user_service.model.TokenPair;
import com.nxt.user_service.model.VerifiedIdentity;
import com.nxt.user_service.repo.RefreshTokenRepository;
import com.nxt.user_service.repo.UserRepository;
import com.nxt.user_service.service.token.IdentityResolutionService;
import com.nxt.user_service.service.token.JwtTokenService;
import com.nxt.user_service.service.token.RefreshTokenFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityResolutionService identityResolutionService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenFactory refreshTokenFactory;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationMethod googleAuthenticationMethod;

    @Nested
    class GoogleAuthentication {

        private AuthService authService;
        private User user;

        @BeforeEach
        void setup() {
            user = new User();
            user.setId(10L);
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setEmail("john@example.com");

            when(googleAuthenticationMethod.provider()).thenReturn(AuthProvider.GOOGLE);
            when(googleAuthenticationMethod.authenticate(any(AuthRequest.class)))
                    .thenReturn(new VerifiedIdentity(
                            "John",
                            "Doe",
                            "john@example.com",
                            "external-id",
                            AuthProvider.GOOGLE
                    ));

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(googleAuthenticationMethod)
            );
        }

        @Test
        void shouldReturnLoginRespSuccessfullyWhenIdTokenValid() {
            AuthRequest req = new GoogleAuthRequest("valid-id-token");
            TokenPair tokenPair = new TokenPair("access-token", "refresh-token");

            when(identityResolutionService.resolveOrCreateUser(any(VerifiedIdentity.class))).thenReturn(user);
            when(jwtTokenService.generateTokens(user)).thenReturn(tokenPair);
            when(refreshTokenFactory.fromRaw(any(), any())).thenReturn(new RefreshToken());

            LoginRespDTO resp = authService.authenticate(req);

            assertNotNull(resp.getRefreshToken());
            assertNotNull(resp.getAccessToken());

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    class PasswordAuthentication {

        private AuthService authService;
        private User user;

        @Mock
        private AuthenticationMethod passwordAuthenticationMethod;

        @BeforeEach
        void setup() {
            user = new User();
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setId(10L);
            user.setEmail("john@example.com");

            when(passwordAuthenticationMethod.provider()).thenReturn(AuthProvider.PASSWORD);
            when(passwordAuthenticationMethod.authenticate(any(AuthRequest.class)))
                    .thenReturn(new VerifiedIdentity(
                            "John",
                            "Doe",
                            "john@example.com",
                            "external-id",
                            AuthProvider.PASSWORD
                    ));

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(passwordAuthenticationMethod)
            );
        }

        @Test
        void shouldReturnLoginRespSuccessfullyWhenCredentialsValid() {
            AuthRequest req = new PasswordAuthRequest("john@example", "strong-password");
            TokenPair tokenPair = new TokenPair("access-token", "refresh-token");

            when(identityResolutionService.resolveOrCreateUser(any(VerifiedIdentity.class))).thenReturn(user);
            when(jwtTokenService.generateTokens(user)).thenReturn(tokenPair);
            when(refreshTokenFactory.fromRaw(any(), any())).thenReturn(new RefreshToken());

            LoginRespDTO resp = authService.authenticate(req);

            assertNotNull(resp.getAccessToken());
            assertNotNull(resp.getRefreshToken());

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    class UnsupportedProvider {

        private AuthService authService;

        @BeforeEach
        void setup() {
            when(googleAuthenticationMethod.provider()).thenReturn(AuthProvider.GOOGLE);

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(googleAuthenticationMethod)
            );
        }

        @Test
        void shouldFailFastWhenProviderIsUnsupported() {
            AuthRequest req = new PasswordAuthRequest("a@b.com", "password");

            assertThrows(
                    UnsupportedAuthProviderException.class,
                    () -> authService.authenticate(req)
            );

            verify(refreshTokenRepository, never()).save(any());
            verify(jwtTokenService, never()).generateTokens(any());
            verify(identityResolutionService, never()).resolveOrCreateUser(any());
        }
    }

    @Nested
    class AuthenticationMethodFailure {

        private AuthService authService;

        @Mock
        private AuthenticationMethod failingAuthMethod;

        @BeforeEach
        void setup() {
            when(failingAuthMethod.provider()).thenReturn(AuthProvider.PASSWORD);
            when(failingAuthMethod.authenticate(any(AuthRequest.class))).thenThrow(new RuntimeException());

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(failingAuthMethod)
            );
        }

        @Test
        void shouldFailFastWhenAuthenticationFails() {
            AuthRequest req = new PasswordAuthRequest("a@b.com", "password");

            assertThrows(
                    RuntimeException.class,
                    () -> authService.authenticate(req)
            );

            verify(refreshTokenRepository, never()).save(any());
            verify(jwtTokenService, never()).generateTokens(any());
            verify(identityResolutionService, never()).resolveOrCreateUser(any());
        }
    }

    @Nested
    class IdentityResolutionFailure {

        private AuthService authService;

        @Mock
        private AuthenticationMethod authenticationMethod;

        @BeforeEach
        void setup() {
            VerifiedIdentity verifiedIdentity = new VerifiedIdentity(
                    "John",
                    "Doe",
                    "john@example.com",
                    "external-id",
                    AuthProvider.PASSWORD
            );

            when(authenticationMethod.provider()).thenReturn(AuthProvider.PASSWORD);
            when(authenticationMethod.authenticate(any(AuthRequest.class))).thenReturn(verifiedIdentity);
            when(identityResolutionService.resolveOrCreateUser(any(VerifiedIdentity.class)))
                    .thenThrow(new RuntimeException());

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(authenticationMethod)
            );
        }

        @Test
        void shouldFailFastWhenIdentityResolutionFails() {
            AuthRequest req = new PasswordAuthRequest("john@example.com", "strong-password");

            assertThrows(
                    RuntimeException.class,
                    () -> authService.authenticate(req)
            );

            verify(refreshTokenRepository, never()).save(any());
            verify(jwtTokenService, never()).generateTokens(any());
        }
    }

    @Nested
    class AuthenticationJwtTokenGenerationFailure {
        private AuthService authService;

        @Mock
        private AuthenticationMethod authenticationMethod;

        @BeforeEach
        void setup() {
            User user = new User();
            user.setId(10L);
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setEmail("john@example.com");

            VerifiedIdentity verifiedIdentity = new VerifiedIdentity(
                    "John",
                    "Doe",
                    "john@example.com",
                    "external-id",
                    AuthProvider.PASSWORD
            );

            when(authenticationMethod.provider()).thenReturn(AuthProvider.PASSWORD);
            when(authenticationMethod.authenticate(any(AuthRequest.class))).thenReturn(verifiedIdentity);
            when(identityResolutionService.resolveOrCreateUser(any(VerifiedIdentity.class))).thenReturn(user);
            when(jwtTokenService.generateTokens(user)).thenThrow(new RuntimeException());

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(authenticationMethod)
            );
        }

        @Test
        void shouldFailFastWhenJwtTokenGenerationFails() {
            AuthRequest req = new PasswordAuthRequest("john@example.com", "strong-password");

            assertThrows(
                    RuntimeException.class,
                    () -> authService.authenticate(req)
            );

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    class RefreshTokenRequest {

        private AuthService authService;

        @Mock
        private AuthenticationMethod authenticationMethod;

        @BeforeEach
        void setup() {
            when(authenticationMethod.provider()).thenReturn(AuthProvider.PASSWORD);

            authService = new AuthService(
                    userRepository,
                    jwtTokenService,
                    refreshTokenRepository,
                    refreshTokenFactory,
                    identityResolutionService,
                    List.of(authenticationMethod)
            );
        }

        @Nested
        class JWTDecodingFailure {

            @Test
            void shouldFailFastWhenJwtDecodingFails() {
                String req = "current-refresh-token";

                when(jwtTokenService.decodeJWT(req)).thenThrow(new RuntimeException());

                assertThrows(
                        RuntimeException.class,
                        () -> authService.refresh(req)
                );

                verify(userRepository, never()).findById(any());
                verify(jwtTokenService, never()).generateTokens(any());
                verify(refreshTokenRepository, never()).save(any());
            }
        }

        @Nested
        class UserRepositoryFailure {

            private DecodedJWT decodedJWT;

            @BeforeEach
            void setup() {
                decodedJWT = mock(DecodedJWT.class);
                when(decodedJWT.getSubject()).thenReturn("10");
            }

            @Test
            void shouldFailFastWhenUserRetrievalFails() {
                String req = "current-refresh-token";

                when(jwtTokenService.decodeJWT(req)).thenReturn(decodedJWT);
                when(userRepository.findById(any())).thenThrow(new UserNotFoundException("User not found"));

                assertThrows(
                        UserNotFoundException.class,
                        () -> authService.refresh(req)
                );

                verify(jwtTokenService, never()).generateTokens(any());
                verify(refreshTokenRepository, never()).save(any());
            }
        }

        @Nested
        class RefreshJwtTokenGenerationFailure {

            private DecodedJWT decodedJWT;
            private User user;

            @BeforeEach
            void setup() {
                decodedJWT = mock(DecodedJWT.class);
                when(decodedJWT.getSubject()).thenReturn("10");

                user = new User();
                user.setId(10L);
                user.setFirstName("John");
                user.setLastName("Doe");
                user.setEmail("john@example.com");
            }

            @Test
            void shouldFailFastWhenTokenGenerationFails() {
                String req = "current-refresh-token";

                when(jwtTokenService.decodeJWT(req)).thenReturn(decodedJWT);
                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(jwtTokenService.generateTokens(user)).thenThrow(new RuntimeException());

                assertThrows(
                        RuntimeException.class,
                        () -> authService.refresh(req)
                );

                verify(refreshTokenRepository, never()).save(any());
            }
        }

        @Nested
        class HappyPath {

            private DecodedJWT decodedJWT;
            private User user;

            @BeforeEach
            void setup() {
                decodedJWT = mock(DecodedJWT.class);
                when(decodedJWT.getSubject()).thenReturn("10");

                user = new User();
                user.setId(10L);
                user.setFirstName("John");
                user.setLastName("Doe");
                user.setEmail("john@example.com");
            }

            @Test
            void shouldReturnLoginRespSuccessfullyWhenRefreshTokenValid() {
                String req = "current-valid-refresh-token";

                when(jwtTokenService.decodeJWT(req)).thenReturn(decodedJWT);
                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(jwtTokenService.generateTokens(user))
                        .thenReturn(new TokenPair(
                                    "access-token",
                                    "refresh-token"
                                   )
                        );

                LoginRespDTO resp = authService.refresh(req);

                assertNotNull(resp.getAccessToken());
                assertNotNull(resp.getRefreshToken());

                verify(userRepository).findById(any());
                verify(jwtTokenService).generateTokens(any());
                verify(refreshTokenRepository).save(any());
            }
        }
    }
}