package com.rashid.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(

        @NotBlank
        String body,

        boolean internal
) {
}