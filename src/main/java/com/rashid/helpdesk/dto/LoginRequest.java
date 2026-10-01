package com.rashid.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String tenantSlug,

        @NotBlank
        String email,

        @NotBlank
        String password
) {
}