package com.rashid.helpdesk.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorId,
        String authorName,
        String body,
        boolean internal,
        Instant createdAt
) {
}