package com.nxt.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegistrationTypeValidator.class)
public @interface ValidRegistrationType {
    String message() default "Invalid registration type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}