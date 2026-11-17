package com.rashid.helpdesk.dto;

import com.rashid.helpdesk.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank
        String fullName,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, message = "password must be at least 8 characters")
        String password,

        @NotNull
        Role role
) {
}