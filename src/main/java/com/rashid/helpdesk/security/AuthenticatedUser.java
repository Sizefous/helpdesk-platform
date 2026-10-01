package com.rashid.helpdesk.security;

import java.util.UUID;

/**
 * Represents "who is making this request" once a JWT has been validated.
 * This becomes the "principal" inside Spring Security's Authentication object,
 * so controllers can retrieve it without ever touching the raw token or headers.
 */
public record AuthenticatedUser(
        UUID userId,
        UUID tenantId,
        String email
) {}