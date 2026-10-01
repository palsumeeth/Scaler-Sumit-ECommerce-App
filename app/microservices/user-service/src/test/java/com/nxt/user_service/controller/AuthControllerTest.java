package com.nxt.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.user_service.config.TestSecurityConfig;
import com.nxt.user_service.dto.GoogleLoginReqDTO;
import com.nxt.user_service.dto.LoginReqDTO;
import com.nxt.user_service.service.AuthService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    class PasswordLogin {

        @Test
        void shouldReturn400WhenEmailIdIsNull() throws Exception {
            LoginReqDTO req = new LoginReqDTO();
            req.setPassword("password");

            mockMvc.perform(
                    post("/api/v1/auth/login/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            ).andExpect(status().isBadRequest());
        }

        @Nested
        class EmailValidity {

            @Test
            void shouldReturn400WhenEmailIdInvalid() throws Exception {
                LoginReqDTO req = new LoginReqDTO();
                req.setEmail("abc");
                req.setPassword("strong-password");

                mockMvc.perform(
                        post("/api/v1/auth/login/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn200WhenEmailIdWithoutDotInDomain() throws Exception {
                LoginReqDTO req = new LoginReqDTO();
                req.setEmail("admin@mailserver");
                req.setPassword("strong-password");

                mockMvc.perform(
                        post("/api/v1/auth/login/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isOk());
            }
        }

        @Nested
        class PasswordValidity {

            @Test
            void shouldReturn400WhenPasswordIsNull() throws Exception {
                LoginReqDTO req = new LoginReqDTO();
                req.setEmail("john@example.com");

                mockMvc.perform(
                        post("/api/v1/auth/login/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn400WhenPasswordIsBlank() throws Exception {
                LoginReqDTO req = new LoginReqDTO();
                req.setEmail("john@example.com");
                req.setPassword("");

                mockMvc.perform(
                        post("/api/v1/auth/login/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn400WhenPasswordTooShort() throws Exception {
                LoginReqDTO req = new LoginReqDTO();
                req.setEmail("john@example.com");
                req.setPassword("abc@123");

                mockMvc.perform(
                        post("/api/v1/auth/login/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturn400WhenPasswordExceedsMaxLength() throws Exception {
                LoginReqDTO req = new LoginReqDTO();
                req.setEmail("john@example.com");
                req.setPassword("a".repeat(73));

                mockMvc.perform(
                        post("/api/v1/auth/login/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                ).andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    class GoogleLogin {

        @Test
        void shouldReturn200WhenIdTokenValid() throws Exception {
            GoogleLoginReqDTO req = new GoogleLoginReqDTO();
            req.setIdToken("valid-id-token");

            mockMvc.perform(
                    post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            ).andExpect(status().isOk());
        }

        @Test
        void shouldReturn400WhenIdTokenIsNull() throws Exception {
            GoogleLoginReqDTO req = new GoogleLoginReqDTO();

            mockMvc.perform(
                    post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            ).andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenIdTokenIsBlank() throws Exception {
            GoogleLoginReqDTO req = new GoogleLoginReqDTO();
            req.setIdToken("");

            mockMvc.perform(
                    post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
            ).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class RefreshTokenRequest {

        @Test
        void shouldReturn200WhenRefreshTokenIsValid() throws Exception {
            String param = "valid-refresh-token";

            mockMvc.perform(
                    post("/api/v1/auth/refresh?refreshToken=" + param)
            ).andExpect(status().isOk());
        }

        @Test
        void shouldReturn400WhenRefreshTokenIsMissing() throws Exception {
            mockMvc.perform(
                    post("/api/v1/auth/refresh")
            ).andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenRefreshTokenIsBlank() throws Exception {
            mockMvc.perform(
                    post("/api/v1/auth/refresh?refreshToken=")
            ).andExpect(status().isBadRequest());
        }
    }
}