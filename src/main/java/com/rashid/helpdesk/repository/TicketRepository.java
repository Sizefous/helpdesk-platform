package com.rashid.helpdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.rashid.helpdesk.entity.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByIdAndTenantId(UUID id, UUID tenantId);

    @Override
    @EntityGraph(attributePaths = {"createdBy", "assignedAgent"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);
}
