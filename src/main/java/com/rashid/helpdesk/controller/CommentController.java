package com.rashid.helpdesk.controller;

import com.rashid.helpdesk.dto.CommentCreateRequest;
import com.rashid.helpdesk.dto.CommentResponse;
import com.rashid.helpdesk.service.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// NOTE: tenant read from X-Tenant-Id header until Phase 2 JWT auth lands.
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(tenantId, ticketId, request));
    }

    @GetMapping
    public List<CommentResponse> list(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ticketId) {
        return commentService.listForTicket(tenantId, ticketId);
    }
}