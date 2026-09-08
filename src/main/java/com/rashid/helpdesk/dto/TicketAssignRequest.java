package com.rashid.helpdesk.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TicketAssignRequest(
        @NotNull
        UUID agentUserId
) {
}
