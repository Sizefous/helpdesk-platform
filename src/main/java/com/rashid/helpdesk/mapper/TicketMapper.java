package com.rashid.helpdesk.mapper;


import org.springframework.stereotype.Component;

import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        User agent = ticket.getAssignedAgent();
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getFullName(),
                agent != null ? agent.getId() : null,
                agent != null ? agent.getFullName() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
