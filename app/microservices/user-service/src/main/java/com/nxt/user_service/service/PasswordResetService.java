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
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final TokenHashService tokenHashService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetRepository passwordResetRepository,
                                PasswordEncoder passwordEncoder,
                                EmailSender emailSender,
                                TokenHashService tokenHashService) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.tokenHashService = tokenHashService;
    }

    @Transactional
    public void requestReset(PasswordResetReqDTO req) {
        long start = System.currentTimeMillis();

        String email = req.getEmail();

        var opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return;
        }

        User user = opt.get();

        // generate raw token
        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
        var expiresAt = Instant.now().plusSeconds(3600);
        var tokenHash = tokenHashService.hashToken(rawToken);

        passwordResetRepository.save(
                PasswordResetToken.from(tokenHash, user.getId(), expiresAt)
        );


        // send email (safe)
        String resetLink = "https://nxt-frontend/reset-password?token=" + rawToken;
        emailSender.sendPasswordReset(email, resetLink);

        long duration = System.currentTimeMillis() - start;
    }

    @Transactional
    public void confirmReset(PasswordResetConfirmDTO req) {
        long start = System.currentTimeMillis();

        String token = req.getToken();


        if (token == null || token.isBlank()) {
            throw new TokenMissingException("Missing reset token");
        }

        var hashed = tokenHashService.hashToken(token);
        var opt = passwordResetRepository.findByTokenHash(hashed);

        if (opt.isEmpty()) {
            throw new InvalidTokenException("Invalid or unrecognized reset token");
        }

        var tokenEntity = opt.get();

        if (tokenEntity.isUsed() || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token expired or used");
        }

        var userOpt = userRepository.findById(tokenEntity.getUserId());
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = userOpt.get();

        // update password
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // mark used + cleanup
        tokenEntity.setUsed(true);
        passwordResetRepository.save(tokenEntity);
        passwordResetRepository.deleteByUserId(user.getId());

        long duration = System.currentTimeMillis() - start;
    }
}