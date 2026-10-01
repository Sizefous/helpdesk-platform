package com.rashid.helpdesk.dto;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        UUID tenantId,
        String role,
        String email
) {
}