package com.nxt.user_service.service;

import com.nxt.user_service.auth.AuthProvider;
import com.nxt.user_service.auth.AuthRequest;
import com.nxt.user_service.auth.AuthenticationMethod;
import com.nxt.user_service.dto.LoginRespDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    private final Map<AuthProvider, AuthenticationMethod> methods;

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenFactory refreshTokenFactory;
    private final IdentityResolutionService identityResolutionService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       JwtTokenService jwtTokenService,
                       RefreshTokenRepository refreshTokenRepository,
                       RefreshTokenFactory refreshTokenFactory,
                       IdentityResolutionService identityResolutionService,
                       List<AuthenticationMethod> authMethods) {

        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenFactory = refreshTokenFactory;
        this.identityResolutionService = identityResolutionService;
        this.methods = authMethods.stream()
                .collect(Collectors.toMap(
                        AuthenticationMethod::provider,
                        Function.identity()
                ));
    }

    public LoginRespDTO authenticate(AuthRequest req) {
        long start = System.currentTimeMillis();

        AuthenticationMethod method = methods.get(req.provider());
        if (method == null) {
            throw new UnsupportedAuthProviderException(req.provider());
        }

        VerifiedIdentity identity = method.authenticate(req);

        User user = identityResolutionService.resolveOrCreateUser(identity);

        TokenPair tokenPair = jwtTokenService.generateTokens(user);
        refreshTokenRepository.save(refreshTokenFactory.fromRaw(tokenPair.refreshToken(), user.getId()));

        long end = System.currentTimeMillis();
        long duration = end - start;

        return LoginRespDTO.from(user, tokenPair);
    }

    public LoginRespDTO refresh(String refreshToken) {
        long start = System.currentTimeMillis();

        String tokenPrefix = (refreshToken == null || refreshToken.isBlank())
                ? "<empty>"
                : refreshToken.substring(0, Math.min(6, refreshToken.length()));
        // TODO: Use this prefix for logging

        var decoded = jwtTokenService.decodeJWT(refreshToken);
        Long userId = Long.parseLong(decoded.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User not found: " + userId);
                });

        TokenPair newTokens = jwtTokenService.generateTokens(user);
        refreshTokenRepository.save(refreshTokenFactory.fromRaw(newTokens.refreshToken(), userId));

        long end = System.currentTimeMillis();
        long duration = end - start;
        // TODO: Find out whether calculating duration should happen before or after the final mapping below.

        LoginRespDTO resp = new LoginRespDTO();
        resp.setUserId(user.getId());
        resp.setAccessToken(newTokens.accessToken());
        resp.setRefreshToken(newTokens.refreshToken());
        resp.setMessage("Token refreshed");

        return resp;
    }

    @Transactional
    public void logout(String refreshToken) {
        var decoded = jwtTokenService.decodeJWT(refreshToken);
        Long userId = Long.parseLong(decoded.getSubject());
        refreshTokenRepository.revokeActiveByUserId(userId);
    }
}