package com.nxt.user_service.service.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nxt.user_service.exception.OAuthInvalidTokenException;
import com.nxt.user_service.exception.OAuthMalformedTokenException;
import com.nxt.user_service.exception.OAuthProviderException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleOAuthVerifier implements OAuthVerifier {

    // TODO: Google CLIENT_ID pending..
    private static final String CLIENT_ID = "<empty-for-now>";

    @Override
    public boolean supports(String provider) {
        return "google".equalsIgnoreCase(provider);
    }

    @Override
    public OAuthClaims verify(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new OAuthMalformedTokenException("Google ID token is missing", null);
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new OAuthInvalidTokenException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            return new OAuthClaims(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("given_name"),
                    (String) payload.get("family_name"),
                    Boolean.TRUE.equals(payload.getEmailVerified())
            );
        } catch (OAuthInvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuthProviderException("Google verification failed", e);
        }
    }
}