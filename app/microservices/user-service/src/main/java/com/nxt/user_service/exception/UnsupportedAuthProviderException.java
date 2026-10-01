package com.nxt.user_service.exception;

import com.nxt.user_service.auth.AuthProvider;

public class UnsupportedAuthProviderException extends RuntimeException {

    public UnsupportedAuthProviderException(AuthProvider authProvider) {
        super(authProvider + " is not supported or doesn't exist");
    }
}
