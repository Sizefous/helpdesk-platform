package com.rashid.helpdesk.controller;

import com.rashid.helpdesk.dto.TicketAssignRequest;
import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.dto.TicketStatusUpdateRequest;
import com.rashid.helpdesk.security.AuthenticatedUser;
import com.rashid.helpdesk.service.ticket.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody TicketCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.create(principal.tenantId(), principal.userId(), request));
    }

    @GetMapping
    public List<TicketResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ticketService.listForTenant(principal.tenantId());
    }

    @GetMapping("/{ticketId}")
    public TicketResponse get(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID ticketId) {
        return ticketService.getForTenant(principal.tenantId(), ticketId);
    }

    @PatchMapping("/{ticketId}/assign")
    public TicketResponse assign(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketAssignRequest request) {
        return ticketService.assign(principal.tenantId(), ticketId, request.agentUserId());
    }

    @PatchMapping("/{ticketId}/status")
    public TicketResponse updateStatus(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketStatusUpdateRequest request) {
        return ticketService.updateStatus(principal.tenantId(), ticketId, request.status());
    }
}