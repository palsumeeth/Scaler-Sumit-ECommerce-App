package com.nxt.user_service.dto;

import com.nxt.user_service.entity.User;
import com.nxt.user_service.model.TokenPair;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LoginRespDTO {
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private String message;

    public static LoginRespDTO from(User user, TokenPair pair) {
        LoginRespDTO resp = new LoginRespDTO();
        resp.setUserId(user.getId());
        resp.setAccessToken(pair.accessToken());
        resp.setRefreshToken(pair.refreshToken());
        resp.setMessage("Login successful");
        return resp;
    }
}