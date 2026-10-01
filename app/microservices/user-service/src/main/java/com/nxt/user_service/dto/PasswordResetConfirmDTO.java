package com.nxt.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmDTO {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
