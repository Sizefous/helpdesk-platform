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
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl service;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = TestData.tenant();
    }

    private UserCreateRequest requestWithRole(Role role) {
        return new UserCreateRequest("Sam Agent", "sam@acme.com", "agentpass123", role);
    }

    // ---- happy path ----

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"AGENT", "CUSTOMER"})
    void create_allowedRole_savesUserInCallersTenantWithHashedPassword(Role role) {
        UserCreateRequest request = requestWithRole(role);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.existsByTenantIdAndEmail(tenant.getId(), "sam@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("agentpass123")).thenReturn("hashed-pw");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), Instant.now());
        });

        UserResponse response = service.create(tenant.getId(), request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getTenant()).isSameAs(tenant);
        assertThat(saved.getEmail()).isEqualTo("sam@acme.com");
        assertThat(saved.getFullName()).isEqualTo("Sam Agent");
        assertThat(saved.getRole()).isEqualTo(role);
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-pw").isNotEqualTo("agentpass123");

        assertThat(response.role()).isEqualTo(role);
        assertThat(response.email()).isEqualTo("sam@acme.com");
    }

    // ---- role restriction ----

    @Test
    void create_adminRole_throwsBadRequestBeforeTouchingAnything() {
        assertThatThrownBy(() -> service.create(tenant.getId(), requestWithRole(Role.ADMIN)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("AGENT or CUSTOMER");

        verifyNoInteractions(userRepository, tenantRepository, passwordEncoder, userMapper);
    }

    // ---- failure paths ----

    @Test
    void create_unknownTenant_throwsNotFoundAndSavesNothing() {
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(tenant.getId(), requestWithRole(Role.AGENT)))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).saveAndFlush(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void create_duplicateEmailInTenant_throwsConflictWithoutHashingOrSaving() {
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.existsByTenantIdAndEmail(tenant.getId(), "sam@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(tenant.getId(), requestWithRole(Role.AGENT)))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).saveAndFlush(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void create_uniqueConstraintRaceAfterExistsCheck_isTranslatedToConflict() {
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.existsByTenantIdAndEmail(tenant.getId(), "sam@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("agentpass123")).thenReturn("hashed-pw");
        // Simulates a concurrent request inserting the same email between the check and the insert.
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("uq_users_tenant_email"));

        assertThatThrownBy(() -> service.create(tenant.getId(), requestWithRole(Role.AGENT)))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(userMapper);
    }
}