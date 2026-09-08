package com.rashid.helpdesk.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rashid.helpdesk.dto.TicketAssignRequest;
import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.dto.TicketStatusUpdateRequest;
import com.rashid.helpdesk.service.ticket.TicketService;

import java.util.List;
import java.util.UUID;

// NOTE: tenant is read from X-Tenant-Id header as a temporary stand-in.
// Phase 2 replaces this with tenant context derived from the authenticated JWT.
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody TicketCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(tenantId, request));
    }

    @GetMapping
    public List<TicketResponse> list(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return ticketService.listForTenant(tenantId);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse get(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ticketId) {
        return ticketService.getForTenant(tenantId, ticketId);
    }

    @PatchMapping("/{ticketId}/assign")
    public TicketResponse assign(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketAssignRequest request) {
        return ticketService.assign(tenantId, ticketId, request.agentUserId());
    }

    @PatchMapping("/{ticketId}/status")
    public TicketResponse updateStatus(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketStatusUpdateRequest request) {
        return ticketService.updateStatus(tenantId, ticketId, request.status());
    }
}
