package com.nxt.user_service.auth;

import com.nxt.user_service.model.VerifiedIdentity;

public interface AuthenticationMethod {
    AuthProvider provider();
    VerifiedIdentity authenticate(AuthRequest request);
}