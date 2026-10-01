package com.nxt.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginReqDTO {

    @NotBlank
    private String idToken;
}
