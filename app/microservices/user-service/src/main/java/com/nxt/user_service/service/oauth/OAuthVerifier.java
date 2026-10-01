package com.nxt.user_service.service.oauth;

public interface OAuthVerifier {
    boolean supports(String provider);
    OAuthClaims verify(String idToken);
}
