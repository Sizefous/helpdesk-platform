package com.rashid.helpdesk.service.ticket;

import java.util.List;
import java.util.UUID;

import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.enums.TicketStatus;

public interface TicketService {
    
    TicketResponse create(UUID tenantId, TicketCreateRequest request);

    List<TicketResponse> listForTenant(UUID tenantId);

    TicketResponse getForTenant(UUID tenantId, UUID ticketId);

    TicketResponse assign(UUID tenantId, UUID ticketId, UUID agentUserId);

    TicketResponse updateStatus(UUID tenantId, UUID ticketId, TicketStatus status);

}
