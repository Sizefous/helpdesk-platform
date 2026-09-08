package com.rashid.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rashid.helpdesk.entity.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByTenantId(UUID tenantId);

    Optional<Ticket> findByIdAndTenantId(UUID id, UUID tenantId);
}
