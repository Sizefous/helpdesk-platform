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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantRegistrationServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TenantRegistrationServiceImpl service;

    private final TenantRegistrationRequest request = new TenantRegistrationRequest(
            "Acme Corp", "acme", "Jane Doe", "jane@acme.com", "secretpass123");

    @Test
    void register_newSlug_createsTenantAndAdminAndReturnsToken() {
        UUID tenantId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(tenantRepository.existsBySlug("acme")).thenReturn(false);
        // The service uses save()'s return value, so the mock must return an entity with an id.
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> {
            Tenant tenant = inv.getArgument(0);
            tenant.setId(tenantId);
            return tenant;
        });
        when(passwordEncoder.encode("secretpass123")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(adminId);
            return user;
        });
        when(jwtService.generateToken(adminId, tenantId, "ADMIN", "jane@acme.com")).thenReturn("jwt-token");

        TenantRegistrationResponse response = service.register(request);

        assertThat(response.tenantId()).isEqualTo(tenantId);
        assertThat(response.companyName()).isEqualTo("Acme Corp");
        assertThat(response.slug()).isEqualTo("acme");
        assertThat(response.adminUserId()).isEqualTo(adminId);
        assertThat(response.adminEmail()).isEqualTo("jane@acme.com");
        assertThat(response.token()).isEqualTo("jwt-token");

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        assertThat(tenantCaptor.getValue().getName()).isEqualTo("Acme Corp");
        assertThat(tenantCaptor.getValue().getSlug()).isEqualTo("acme");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedAdmin = userCaptor.getValue();
        assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(savedAdmin.getTenant().getId()).isEqualTo(tenantId);
        assertThat(savedAdmin.getFullName()).isEqualTo("Jane Doe");
        assertThat(savedAdmin.getPasswordHash()).isEqualTo("hashed-pw").isNotEqualTo("secretpass123");
    }

    @Test
    void register_duplicateSlug_throwsConflictAndSavesNothing() {
        when(tenantRepository.existsBySlug("acme")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("acme");

        verify(tenantRepository, never()).save(any());
        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }
}