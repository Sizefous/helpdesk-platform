package com.rashid.helpdesk.dto;

import com.rashid.helpdesk.enums.TicketStatus;

import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateRequest(
        @NotNull
        TicketStatus status
) {
}
