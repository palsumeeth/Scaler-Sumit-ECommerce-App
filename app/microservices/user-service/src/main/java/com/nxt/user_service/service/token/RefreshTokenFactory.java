package com.nxt.user_service.service.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.nxt.user_service.entity.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenFactory {
    private final JwtTokenService jwtTokenService;
    private final TokenHashService tokenHashService;

    @Autowired
    public RefreshTokenFactory(JwtTokenService jwtTokenService, TokenHashService tokenHashService) {
        this.jwtTokenService = jwtTokenService;
        this.tokenHashService = tokenHashService;
    }

    public RefreshToken fromRaw(String rawToken, Long userId) {
        DecodedJWT decodedJWT = jwtTokenService.decodeJWT(rawToken);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setJti(decodedJWT.getId());
        rt.setExpiresAt(decodedJWT.getExpiresAtAsInstant());
        rt.setTokenHash(tokenHashService.hashToken(rawToken));
        rt.setRevoked(false);
        return rt;
    }
}