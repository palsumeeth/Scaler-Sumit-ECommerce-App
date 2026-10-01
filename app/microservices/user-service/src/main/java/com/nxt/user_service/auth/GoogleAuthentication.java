package com.nxt.user_service.auth;

import com.nxt.user_service.model.VerifiedIdentity;
import com.nxt.user_service.service.oauth.GoogleOAuthVerifier;
import com.nxt.user_service.service.oauth.OAuthClaims;
import com.nxt.user_service.service.oauth.OAuthVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleAuthentication implements AuthenticationMethod {

    private final OAuthVerifier oAuthVerifier;

    @Autowired
    public GoogleAuthentication(GoogleOAuthVerifier googleOAuthVerifier) {
        this.oAuthVerifier = googleOAuthVerifier;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public VerifiedIdentity authenticate(AuthRequest request) {
        GoogleAuthRequest req = (GoogleAuthRequest) request;

        OAuthClaims claims = oAuthVerifier.verify(req.idToken());

        return new VerifiedIdentity(
                claims.firstName(),
                claims.lastName(),
                claims.email(),
                claims.subject(),
                AuthProvider.GOOGLE
        );
    }
}