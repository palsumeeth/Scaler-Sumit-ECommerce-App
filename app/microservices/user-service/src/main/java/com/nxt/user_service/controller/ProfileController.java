package com.nxt.user_service.controller;

import com.nxt.user_service.dto.UpdateProfileDTO;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.security.CustomUserPrincipal;
import com.nxt.user_service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService service;

    @Autowired
    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getId();

        User u = service.getById(userId);
        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateProfileDTO dto) {

        Long userId = principal.getId(); // FIX

        User updated = service.update(userId, dto);
        updated.setPasswordHash(null);

        return ResponseEntity.ok(updated);
    }
}