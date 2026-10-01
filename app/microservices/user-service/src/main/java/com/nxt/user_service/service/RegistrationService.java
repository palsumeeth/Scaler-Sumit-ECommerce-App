package com.nxt.user_service.service;

import com.nxt.user_service.dto.CreateUserReqDTO;
import com.nxt.user_service.dto.CreateUserRespDTO;
import com.nxt.user_service.entity.RefreshToken;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.exception.UserAlreadyExistsException;
import com.nxt.user_service.model.ResponseStatus;
import com.nxt.user_service.repo.RefreshTokenRepository;
import com.nxt.user_service.repo.UserRepository;

import com.nxt.user_service.service.token.JwtTokenService;
import com.nxt.user_service.service.token.RefreshTokenFactory;
import com.nxt.user_service.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenFactory refreshTokenFactory;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserActivityPublisher userActivityPublisher;

    @Autowired
    public RegistrationService(UserRepository repository,
                               PasswordEncoder passwordEncoder,
                               JwtTokenService jwtTokenService,
                               RefreshTokenFactory refreshTokenFactory,
                               RefreshTokenRepository refreshTokenRepository,
                               UserActivityPublisher userActivityPublisher) {

        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenFactory = refreshTokenFactory;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userActivityPublisher = userActivityPublisher;
    }

    public CreateUserRespDTO createUser(CreateUserReqDTO req) {
        long start = System.currentTimeMillis();

        // check email, if not continue
        String email = req.getEmail();
        if (userRepository.existsByEmail(email)) {
            // log event
            throw new UserAlreadyExistsException("Email already registered");
        }

        // build user entity
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setRegistrationType(Util.fromString(req.getRegistrationType()));
        user.setMetadata(req.getMetadata());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        user = userRepository.save(user);
        userActivityPublisher.userRegistered(user);

        // generate + persist tokens
        var tokens = jwtTokenService.generateTokens(user);
        RefreshToken refreshTokenEntity = refreshTokenFactory.fromRaw(tokens.refreshToken(), user.getId());
        refreshTokenRepository.save(refreshTokenEntity);

        long duration = System.currentTimeMillis() - start;

        CreateUserRespDTO resp = new CreateUserRespDTO();
        resp.setFirstName(req.getFirstName());
        resp.setLastName(req.getLastName());
        resp.setUserId(user.getId());
        resp.setResponseStatus(ResponseStatus.SUCCESSFUL);
        resp.setEmail(req.getEmail());
        resp.setEmailVerified(false);
        resp.setCreatedAt(new Date().toString());
        resp.setAccessToken(tokens.accessToken());
        resp.setRefreshToken(tokens.refreshToken());
        return resp;
    }
}