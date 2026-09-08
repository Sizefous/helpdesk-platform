package com.rashid.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import com.rashid.helpdesk.enums.TicketPriority;


public record TicketCreateRequest(

        @NotNull
        UUID createdByUserId,

        @NotBlank
        String subject,

        @NotBlank
        String description,

        @NotNull
        TicketPriority priority
) {
}
