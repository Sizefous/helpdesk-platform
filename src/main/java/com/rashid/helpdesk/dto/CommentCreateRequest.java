package com.rashid.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentCreateRequest(

        @NotNull
        UUID authorUserId,

        @NotBlank
        String body,

        boolean internal
) {
}