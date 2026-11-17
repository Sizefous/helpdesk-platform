package com.rashid.helpdesk.service.user;

import com.rashid.helpdesk.common.BadRequestException;
import com.rashid.helpdesk.common.ConflictException;
import com.rashid.helpdesk.common.NotFoundException;
import com.rashid.helpdesk.dto.UserCreateRequest;
import com.rashid.helpdesk.dto.UserResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.mapper.UserMapper;
import com.rashid.helpdesk.repository.TenantRepository;
import com.rashid.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // Allow-list, so a role added to the enum later is not creatable here by accident.
    private static final Set<Role> CREATABLE_ROLES = Set.of(Role.AGENT, Role.CUSTOMER);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse create(UUID tenantId, UserCreateRequest request) {
        if (!CREATABLE_ROLES.contains(request.role())) {
            throw new BadRequestException("Role must be AGENT or CUSTOMER");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        if (userRepository.existsByTenantIdAndEmail(tenantId, request.email())) {
            throw new ConflictException("A user with this email already exists in this tenant");
        }

        User user = User.builder()
                .tenant(tenant)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .build();

        try {
            // saveAndFlush so the INSERT runs inside this method, where the catch below can see it.
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Two simultaneous requests can both pass the exists check; the
            // (tenant_id, email) unique constraint rejects the second one.
            throw new ConflictException("A user with this email already exists in this tenant");
        }

        return userMapper.toResponse(user);
    }
}