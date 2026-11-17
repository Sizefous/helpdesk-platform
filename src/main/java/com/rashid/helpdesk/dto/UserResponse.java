package com.rashid.helpdesk.dto;

import com.rashid.helpdesk.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        Instant createdAt
) {
}