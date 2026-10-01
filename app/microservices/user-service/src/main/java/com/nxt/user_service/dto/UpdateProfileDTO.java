package com.nxt.user_service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileDTO {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    private String email;

    @AssertTrue(message = "At least one field must be provided")
    public boolean hasAtLeastOneField() {
        return firstName != null || lastName != null || email != null;
    }

}
