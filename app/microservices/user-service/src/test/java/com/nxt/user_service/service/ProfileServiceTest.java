package com.nxt.user_service.service;

import com.nxt.user_service.dto.UpdateProfileDTO;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    @BeforeEach
    void setup() {
        user = new User();
        user.setId(10L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
    }

    // return if user exist
    @Test
    void shouldReturnUserWhenIdExists() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = profileService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    // throws if user not found
    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    // updates only provided fields
    @Test
    void shouldUpdateOnlyProvidedFields() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setFirstName("Jane");

        User updated = profileService.update(1L, dto);

        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Doe");
        assertThat(updated.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).save(user);
    }

    // does not overwrite nulls
    // saves updated user
    @Test
    void shouldUpdateAllFieldsWhenProvided() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane@example.com");

        User updated = profileService.update(1L, dto);

        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Smith");
        assertThat(updated.getEmail()).isEqualTo("jane@example.com");
    }
}
