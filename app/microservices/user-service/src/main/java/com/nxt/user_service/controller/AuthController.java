package com.nxt.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.nxt.user_service.auth.GoogleAuthRequest;
import com.nxt.user_service.auth.PasswordAuthRequest;
import com.nxt.user_service.dto.GoogleLoginReqDTO;
import com.nxt.user_service.dto.LoginReqDTO;
import com.nxt.user_service.dto.LoginRespDTO;
import com.nxt.user_service.service.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/password")
    public ResponseEntity<LoginRespDTO> passwordLogin(@RequestBody @Valid LoginReqDTO req) {
        long start = System.currentTimeMillis();

        LoginRespDTO resp = authService.authenticate(
                new PasswordAuthRequest(req.getEmail(), req.getPassword())
        );

        long end = System.currentTimeMillis();
        long duration = end - start;

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login/google")
    public ResponseEntity<LoginRespDTO> googleLogin(@RequestBody @Valid GoogleLoginReqDTO req) {
        long start = System.currentTimeMillis();

        LoginRespDTO resp = authService.authenticate(
                new GoogleAuthRequest(req.getIdToken())
        );

        long end = System.currentTimeMillis();
        long duration = end - start;

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginRespDTO> refresh(
            @RequestParam("refreshToken")
            @NotBlank(message = "refreshToken must not be blank")
            String refreshToken
    ) {
        long start = System.currentTimeMillis();

        LoginRespDTO resp = authService.refresh(refreshToken);

        long end = System.currentTimeMillis();
        long duration = end - start;

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestParam("refreshToken")
            @NotBlank(message = "refreshToken must not be blank")
            String refreshToken
    ) {
        authService.logout(refreshToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}