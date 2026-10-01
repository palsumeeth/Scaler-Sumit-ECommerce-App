package com.nxt.user_service.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nxt.user_service.model.RegistrationType;

public class Util {

    private Util() throws IllegalArgumentException {
        throw new IllegalArgumentException("This is a utility class");
    }

    @JsonCreator
    public static RegistrationType fromString(String value) {
        return RegistrationType.valueOf(value.toUpperCase());
    }
}