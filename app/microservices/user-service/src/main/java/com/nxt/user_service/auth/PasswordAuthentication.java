package com.nxt.user_service.auth;

import com.nxt.user_service.entity.User;
import com.nxt.user_service.exception.InvalidCredentialsException;
import com.nxt.user_service.exception.UserNotFoundException;
import com.nxt.user_service.model.VerifiedIdentity;
import com.nxt.user_service.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordAuthentication implements AuthenticationMethod {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordAuthentication(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.PASSWORD;
    }

    @Override
    public VerifiedIdentity authenticate(AuthRequest request) {
        PasswordAuthRequest req = (PasswordAuthRequest) request;

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return new VerifiedIdentity(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getId().toString(),
                AuthProvider.PASSWORD
        );
    }
}