package com.rashid.helpdesk.dto;

import com.rashid.helpdesk.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketCreateRequest(

        @NotBlank
        String subject,

        @NotBlank
        String description,

        @NotNull
        TicketPriority priority
) {
}