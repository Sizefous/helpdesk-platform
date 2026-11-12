package com.rashid.helpdesk.dto;

import com.rashid.helpdesk.enums.TicketPriority;
import com.rashid.helpdesk.enums.TicketStatus;

import java.util.UUID;

public record TicketFilter(
        TicketStatus status,
        TicketPriority priority,
        UUID assignedAgentId,
        String q
) {
}