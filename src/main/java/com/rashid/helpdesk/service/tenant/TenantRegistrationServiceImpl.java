package com.rashid.helpdesk.service.tenant;

import com.rashid.helpdesk.common.ConflictException;
import com.rashid.helpdesk.dto.TenantRegistrationRequest;
import com.rashid.helpdesk.dto.TenantRegistrationResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.repository.TenantRepository;
import com.rashid.helpdesk.repository.UserRepository;
import com.rashid.helpdesk.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantRegistrationServiceImpl implements TenantRegistrationService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public TenantRegistrationResponse register(TenantRegistrationRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new ConflictException("A tenant with slug '" + request.slug() + "' already exists");
        }

        Tenant tenant = Tenant.builder()
                .name(request.companyName())
                .slug(request.slug())
                .build();
        tenant = tenantRepository.save(tenant);

        User admin = User.builder()
                .tenant(tenant)
                .email(request.adminEmail())
                .passwordHash(passwordEncoder.encode(request.adminPassword()))
                .fullName(request.adminFullName())
                .role(Role.ADMIN)
                .build();
        admin = userRepository.save(admin);

        String token = jwtService.generateToken(
                admin.getId(),
                tenant.getId(),
                admin.getRole().name(),
                admin.getEmail()
        );

        return new TenantRegistrationResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                admin.getId(),
                admin.getEmail(),
                token
        );
    }
}