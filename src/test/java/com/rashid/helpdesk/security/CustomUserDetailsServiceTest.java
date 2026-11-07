package com.rashid.helpdesk.security;

import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.repository.UserRepository;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_existingUser_returnsPrincipalWithHashAndRole() {
        Tenant tenant = TestData.tenant();
        User agent = TestData.user(tenant, Role.AGENT);
        when(userRepository.findByTenantSlugAndEmail("acme", agent.getEmail())).thenReturn(Optional.of(agent));

        UserDetails details = service.loadUserByUsername("acme:" + agent.getEmail());

        assertThat(details.getUsername()).isEqualTo(agent.getEmail());
        assertThat(details.getPassword()).isEqualTo("hashed-password");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_AGENT");
        assertThat(((UserPrincipal) details).getUser()).isSameAs(agent);
    }

    @Test
    void loadUserByUsername_missingSeparator_throwsWithoutQueryingDatabase() {
        assertThatThrownBy(() -> service.loadUserByUsername("no-colon-here"))
                .isInstanceOf(UsernameNotFoundException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_unknownUser_throwsUsernameNotFound() {
        when(userRepository.findByTenantSlugAndEmail("acme", "ghost@acme.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("acme:ghost@acme.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_emailContainingColon_splitsOnFirstColonOnly() {
        Tenant tenant = TestData.tenant();
        User user = TestData.user(tenant, Role.CUSTOMER);
        when(userRepository.findByTenantSlugAndEmail("acme", "odd:name@acme.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("acme:odd:name@acme.com");

        assertThat(((UserPrincipal) details).getUser()).isSameAs(user);
    }
}