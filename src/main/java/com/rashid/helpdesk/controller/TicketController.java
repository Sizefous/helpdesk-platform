package com.rashid.helpdesk.controller;

import com.rashid.helpdesk.common.PageRequests;
import com.rashid.helpdesk.dto.PageResponse;
import com.rashid.helpdesk.dto.TicketAssignRequest;
import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketFilter;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.dto.TicketStatusUpdateRequest;
import com.rashid.helpdesk.enums.TicketPriority;
import com.rashid.helpdesk.enums.TicketStatus;
import com.rashid.helpdesk.security.AuthenticatedUser;
import com.rashid.helpdesk.service.ticket.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
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

        private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "updatedAt");

    @GetMapping
    public PageResponse<TicketResponse> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) UUID assignedAgentId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        TicketFilter filter = new TicketFilter(status, priority, assignedAgentId, q);
        Pageable pageable = PageRequests.of(page, size, sortBy, direction, SORTABLE_FIELDS);
        return ticketService.search(principal.tenantId(), filter, pageable);
    }
}