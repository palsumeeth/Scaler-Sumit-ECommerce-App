package com.nxt.user_service.dto;

import com.nxt.user_service.model.ResponseStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateUserRespDTO {
    private Long userId;
    private ResponseStatus responseStatus;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isEmailVerified;
    private String createdAt;
    private String accessToken;
    private String refreshToken;
}