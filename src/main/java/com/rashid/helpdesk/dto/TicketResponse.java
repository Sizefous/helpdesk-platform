package com.rashid.helpdesk.dto;


import java.time.Instant;
import java.util.UUID;
import com.rashid.helpdesk.enums.TicketPriority;
import com.rashid.helpdesk.enums.TicketStatus;

public record TicketResponse(
        UUID id,
        String subject,
        String description,
        TicketStatus status,
        TicketPriority priority,
        UUID createdByUserId,
        String createdByName,
        UUID assignedAgentId,
        String assignedAgentName,
        Instant createdAt,
        Instant updatedAt
) {
}
