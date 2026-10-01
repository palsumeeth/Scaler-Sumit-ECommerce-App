package com.nxt.user_service.controller;

import com.nxt.user_service.dto.CreateUserReqDTO;
import com.nxt.user_service.dto.CreateUserRespDTO;
import com.nxt.user_service.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/v1/users")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<CreateUserRespDTO> createUser(@Valid @RequestBody CreateUserReqDTO req) {
        long start = System.currentTimeMillis();

        CreateUserRespDTO resp = registrationService.createUser(req);

        long end = System.currentTimeMillis();

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}