package com.nxt.user_service.validation;

import com.nxt.user_service.model.RegistrationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationTypeValidator implements ConstraintValidator<ValidRegistrationType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return false;
        try {
            RegistrationType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}