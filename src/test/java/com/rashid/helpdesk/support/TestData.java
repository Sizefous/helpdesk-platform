package com.rashid.helpdesk.support;

import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.enums.TicketPriority;
import com.rashid.helpdesk.enums.TicketStatus;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public final class TestData {

    public static final String JWT_SECRET = Base64.getEncoder().encodeToString(
            "unit-test-secret-key-that-is-long-enough-for-hs256".getBytes(StandardCharsets.UTF_8));

    public static final String OTHER_JWT_SECRET = Base64.getEncoder().encodeToString(
            "a-completely-different-secret-key-for-hs256-tests".getBytes(StandardCharsets.UTF_8));

    private TestData() {
    }

    public static Tenant tenant() {
        return Tenant.builder()
                .id(UUID.randomUUID())
                .name("Acme Corp")
                .slug("acme")
                .build();
    }

    public static User user(Tenant tenant, Role role) {
        return User.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .email(role.name().toLowerCase() + "@acme.com")
                .passwordHash("hashed-password")
                .fullName("Test " + role.name())
                .role(role)
                .build();
    }

    public static Ticket ticket(Tenant tenant, User createdBy) {
        return Ticket.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .subject("Login page returns 500")
                .description("Server error on the login form")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.HIGH)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}