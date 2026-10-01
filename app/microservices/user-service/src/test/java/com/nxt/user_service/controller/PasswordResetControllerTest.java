package com.nxt.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.user_service.config.TestSecurityConfig;
import com.nxt.user_service.dto.PasswordResetConfirmDTO;
import com.nxt.user_service.dto.PasswordResetReqDTO;
import com.nxt.user_service.service.PasswordResetService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordResetController.class)
@Import(TestSecurityConfig.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordResetService passwordResetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class RequestPasswordReset {

        @Test
        void shouldRequestPasswordResetSuccessfully() throws Exception {
            PasswordResetReqDTO req = new PasswordResetReqDTO();
            req.setEmail("john@example.com");

            mockMvc.perform(
                            post("/api/v1/password/reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isAccepted());

            Mockito.verify(passwordResetService).requestReset(any(PasswordResetReqDTO.class));
        }

        @Test
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            PasswordResetReqDTO req = new PasswordResetReqDTO();
            req.setEmail("not-an-email");

            mockMvc.perform(
                            post("/api/v1/password/reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());

            Mockito.verify(passwordResetService, never()).requestReset(any());
        }

        @Test
        void shouldReturn400WhenEmailIsMissing() throws Exception {
            PasswordResetReqDTO req = new PasswordResetReqDTO();

            mockMvc.perform(
                            post("/api/v1/password/reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());

            Mockito.verify(passwordResetService, never()).requestReset(any());
        }

        @Test
        void shouldReturn404WhenUrlIsBad() throws Exception {
            PasswordResetReqDTO req = new PasswordResetReqDTO();
            req.setEmail("john@example.com");

            mockMvc.perform(
                            post("/api/v1/password/request-reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn415WhenMediaTypeIsUnsupported()  throws Exception {
            String req = "john@example.com";

            mockMvc.perform(
                            post("/api/v1/password/reset")
                                    .contentType(MediaType.TEXT_PLAIN)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        void shouldReturn400WhenPlaintextBodyIsSent()  throws Exception {
            String req = "john@example.com";

            mockMvc.perform(
                            post("/api/v1/password/reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ConfirmPasswordReset {

        @Test
        void shouldConfirmPasswordResetSuccessfully() throws Exception {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setToken("abc123token");
            req.setNewPassword("StrongPass!23");

            mockMvc.perform(
                            post("/api/v1/password/confirm")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            Mockito.verify(passwordResetService).confirmReset(any(PasswordResetConfirmDTO.class));
        }

        @Test
        void shouldReturn400WhenTokenIsMissing() throws Exception {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setNewPassword("SneakyHacker123");


            mockMvc.perform(
                            post("/api/v1/password/confirm")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.token").value("must not be blank"));

            Mockito.verify(passwordResetService, never()).confirmReset(any());
        }

        @Test
        void shouldReturn400WhenNewPasswordMissing() throws Exception {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setToken("abc123token");

            mockMvc.perform(
                            post("/api/v1/password/confirm")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.newPassword").value("must not be blank"));
        }

        @Test
        void shouldReturn404WhenUrlIsBad() throws Exception {
            PasswordResetConfirmDTO req = new PasswordResetConfirmDTO();
            req.setToken("abc123token");
            req.setNewPassword("StrongPass!23");

            mockMvc.perform(
                    post("/api/v1/password/confirm-reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn415WhenMediaTypeIsUnsupported()  throws Exception {
            String req = "john@example.com";

            mockMvc.perform(
                            post("/api/v1/password/confirm")
                                    .contentType(MediaType.TEXT_PLAIN)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        void shouldReturn400WhenPlaintextBodyIsSent()  throws Exception {
            String req = "john@example.com";

            mockMvc.perform(
                            post("/api/v1/password/confirm")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isBadRequest());
        }
    }
}