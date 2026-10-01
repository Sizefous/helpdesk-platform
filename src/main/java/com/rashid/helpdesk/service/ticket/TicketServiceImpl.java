package com.rashid.helpdesk.service.ticket;

import com.rashid.helpdesk.common.NotFoundException;
import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.TicketStatus;
import com.rashid.helpdesk.mapper.TicketMapper;
import com.rashid.helpdesk.repository.TenantRepository;
import com.rashid.helpdesk.repository.TicketRepository;
import com.rashid.helpdesk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponse create(UUID tenantId, UUID createdByUserId, TicketCreateRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Ticket ticket = Ticket.builder()
                .tenant(tenant)
                .subject(request.subject())
                .description(request.description())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .createdBy(createdBy)
                .build();

        ticket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listForTenant(UUID tenantId) {
        return ticketRepository.findByTenantId(tenantId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getForTenant(UUID tenantId, UUID ticketId) {
        Ticket ticket = findTicketOrThrow(tenantId, ticketId);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse assign(UUID tenantId, UUID ticketId, UUID agentUserId) {
        Ticket ticket = findTicketOrThrow(tenantId, ticketId);
        User agent = userRepository.findById(agentUserId)
                .orElseThrow(() -> new NotFoundException("Agent not found"));

        ticket.setAssignedAgent(agent);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(UUID tenantId, UUID ticketId, TicketStatus status) {
        Ticket ticket = findTicketOrThrow(tenantId, ticketId);
        ticket.setStatus(status);
        return ticketMapper.toResponse(ticket);
    }

    private Ticket findTicketOrThrow(UUID tenantId, UUID ticketId) {
        return ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }
}
