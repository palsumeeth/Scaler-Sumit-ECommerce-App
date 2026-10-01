package com.nxt.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.user_service.config.TestSecurityConfig;
import com.nxt.user_service.dto.CreateUserReqDTO;
import com.nxt.user_service.dto.CreateUserRespDTO;
import com.nxt.user_service.exception.UserAlreadyExistsException;
import com.nxt.user_service.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegistrationController.class)
@Import(TestSecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        CreateUserReqDTO req = new CreateUserReqDTO();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("StrongPassword@123");
        req.setRegistrationType("EMAIL");

        CreateUserRespDTO resp = new CreateUserRespDTO();
        resp.setUserId(10L);
        resp.setEmail("john@example.com");

        when(registrationService.createUser(any(CreateUserReqDTO.class)))
                .thenReturn(resp);

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(registrationService).createUser(any(CreateUserReqDTO.class));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        CreateUserReqDTO req = new CreateUserReqDTO(); // empty

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        CreateUserReqDTO req = new CreateUserReqDTO();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("StrongPassword@123");
        req.setRegistrationType("EMAIL");

        when(registrationService.createUser(any()))
                .thenThrow(new UserAlreadyExistsException("Email already registered"));

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }
}