package com.rashid.helpdesk.repository;

import com.rashid.helpdesk.dto.TicketFilter;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.enums.TicketPriority;
import com.rashid.helpdesk.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    /**
     * The only public entry point. The tenant condition is always the first predicate
     * and is mandatory, so a search can never run without tenant scoping.
     */
    public static Specification<Ticket> matching(UUID tenantId, TicketFilter filter) {
        Objects.requireNonNull(tenantId, "tenantId is required");

        return Specification.where(forTenant(tenantId))
                .and(hasStatus(filter.status()))
                .and(hasPriority(filter.priority()))
                .and(assignedTo(filter.assignedAgentId()))
                .and(subjectContains(filter.q()));
    }

    private static Specification<Ticket> forTenant(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    // A null predicate means "no restriction", which is how an absent filter is skipped.
    private static Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Ticket> hasPriority(TicketPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    private static Specification<Ticket> assignedTo(UUID agentId) {
        return (root, query, cb) -> agentId == null ? null : cb.equal(root.get("assignedAgent").get("id"), agentId);
    }

    private static Specification<Ticket> subjectContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return null;
            }
            String pattern = "%" + escapeLike(q.trim().toLowerCase(Locale.ROOT)) + "%";
            return cb.like(cb.lower(root.get("subject")), pattern, '\\');
        };
    }

    // Without this, a user typing % or _ would be using SQL wildcards, not searching for the character.
    private static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}