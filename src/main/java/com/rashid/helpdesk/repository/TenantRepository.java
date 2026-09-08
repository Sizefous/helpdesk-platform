package com.rashid.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rashid.helpdesk.entity.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
