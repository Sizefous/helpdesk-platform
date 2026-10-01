package com.rashid.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rashid.helpdesk.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByTenantSlugAndEmail(String tenantSlug, String email);
}
