package com.nxt.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxt.user_service.config.TestSecurityConfig;
import com.nxt.user_service.dto.UpdateProfileDTO;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.security.CustomUserPrincipal;
import com.nxt.user_service.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(TestSecurityConfig.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private void withAuthenticatedUser(Long userId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, "john@example.com");

        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetCurrentUser {

        @Test
        void shouldReturnUserProfileForAuthenticatedUser() throws Exception {
            withAuthenticatedUser(10L);

            User user = new User();
            user.setId(10L);
            user.setEmail("john@example.com");
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setPasswordHash("secret");

            when(profileService.getById(10L)).thenReturn(user);

            mockMvc.perform(
                    get("/api/v1/profile/me")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(
                        get("/api/v1/profile/me")
                    )
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateCurrentUser {

        @Test
        void shouldUpdateProfileSuccessfully() throws Exception {
            withAuthenticatedUser(10L);

            UpdateProfileDTO dto = new UpdateProfileDTO();
            dto.setFirstName("Jane");
            dto.setLastName("Smith");

            User updatedUser = new User();
            updatedUser.setId(10L);
            updatedUser.setFirstName("Jane");
            updatedUser.setLastName("Smith");
            updatedUser.setEmail("john@example.com");

            when(profileService.update(eq(10L), any(UpdateProfileDTO.class)))
                    .thenReturn(updatedUser);

            mockMvc.perform(
                    put("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"))
                    .andExpect(jsonPath("$.email").value("john@example.com"));
        }

        @Test
        void shouldReturn400WhenUpdatePayloadIsInvalid() throws Exception {
            withAuthenticatedUser(10L);

            UpdateProfileDTO dto = new UpdateProfileDTO();

            mockMvc.perform(
                    put("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn401WhenUserNotAuthenticated() throws Exception {
            UpdateProfileDTO dto = new UpdateProfileDTO();
            dto.setFirstName("Jane");

            mockMvc.perform(
                    put("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))

                    )
                    .andExpect(status().isUnauthorized());
        }
    }
}