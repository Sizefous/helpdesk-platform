package com.rashid.helpdesk.service.ticket;

import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.enums.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    TicketResponse create(UUID tenantId, UUID createdByUserId, TicketCreateRequest request);

    List<TicketResponse> listForTenant(UUID tenantId);

    TicketResponse getForTenant(UUID tenantId, UUID ticketId);

    TicketResponse assign(UUID tenantId, UUID ticketId, UUID agentUserId);

    TicketResponse updateStatus(UUID tenantId, UUID ticketId, TicketStatus status);
}