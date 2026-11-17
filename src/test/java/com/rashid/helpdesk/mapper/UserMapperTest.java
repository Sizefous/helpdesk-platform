package com.rashid.helpdesk.mapper;

import com.rashid.helpdesk.dto.UserResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toResponse_mapsPublicFields() {
        Tenant tenant = TestData.tenant();
        User user = TestData.user(tenant, Role.AGENT);
        user.setCreatedAt(Instant.parse("2026-11-11T10:00:00Z"));

        UserResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.fullName()).isEqualTo(user.getFullName());
        assertThat(response.role()).isEqualTo(Role.AGENT);
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-11-11T10:00:00Z"));
    }
}