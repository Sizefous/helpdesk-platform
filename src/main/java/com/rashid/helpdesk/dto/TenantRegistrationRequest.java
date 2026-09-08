package com.rashid.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantRegistrationRequest(

        @NotBlank
        String companyName,

        @NotBlank
        @Pattern(regexp = "^[a-z0-9-]+$", message = "slug must contain only lowercase letters, digits, and hyphens")
        String slug,

        @NotBlank
        String adminFullName,

        @Email
        @NotBlank
        String adminEmail,

        @NotBlank
        @Size(min = 8, message = "password must be at least 8 characters")
        String adminPassword
) {
}
