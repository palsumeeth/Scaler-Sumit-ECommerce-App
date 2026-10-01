package com.nxt.user_service.controller;

import com.nxt.user_service.dto.PasswordResetConfirmDTO;
import com.nxt.user_service.dto.PasswordResetReqDTO;
import com.nxt.user_service.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetReqDTO req) {
        long start = System.currentTimeMillis();

        passwordResetService.requestReset(req);

        long end = System.currentTimeMillis();

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody PasswordResetConfirmDTO req) {
        long start = System.currentTimeMillis();

        passwordResetService.confirmReset(req);

        long end = System.currentTimeMillis();

        return ResponseEntity.ok().build();
    }
}