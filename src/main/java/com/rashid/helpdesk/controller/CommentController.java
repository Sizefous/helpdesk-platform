package com.rashid.helpdesk.controller;

import com.rashid.helpdesk.dto.CommentCreateRequest;
import com.rashid.helpdesk.dto.CommentResponse;
import com.rashid.helpdesk.security.AuthenticatedUser;
import com.rashid.helpdesk.service.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID ticketId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(principal.tenantId(), ticketId, principal.userId(), request));
    }

    @GetMapping
    public List<CommentResponse> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID ticketId) {
        return commentService.listForTicket(principal.tenantId(), ticketId);
    }
}