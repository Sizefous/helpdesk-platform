package com.rashid.helpdesk.dto;

import java.util.UUID;

public record TenantRegistrationResponse(
        UUID tenantId,
        String companyName,
        String slug,
        UUID adminUserId,
        String adminEmail
) {
}
