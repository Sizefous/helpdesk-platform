package com.rashid.helpdesk.service.auth;

import com.rashid.helpdesk.dto.LoginRequest;
import com.rashid.helpdesk.dto.LoginResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.security.JwtService;
import com.rashid.helpdesk.security.UserPrincipal;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_validCredentials_returnsTokenAndUserInfo() {
        Tenant tenant = TestData.tenant();
        User agent = TestData.user(tenant, Role.AGENT);
        LoginRequest request = new LoginRequest("acme", agent.getEmail(), "plain-password");

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(agent));
        when(jwtService.generateToken(agent.getId(), tenant.getId(), "AGENT", agent.getEmail()))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(agent.getId());
        assertThat(response.tenantId()).isEqualTo(tenant.getId());
        assertThat(response.role()).isEqualTo("AGENT");
        assertThat(response.email()).isEqualTo(agent.getEmail());
    }

    @Test
    void login_packsTenantSlugAndEmailIntoSingleIdentifier() {
        Tenant tenant = TestData.tenant();
        User admin = TestData.user(tenant, Role.ADMIN);
        LoginRequest request = new LoginRequest("acme", admin.getEmail(), "plain-password");

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(admin));
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("jwt-token");

        authService.login(request);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        // This format is the contract with CustomUserDetailsService.
        assertThat(captor.getValue().getPrincipal()).isEqualTo("acme:" + admin.getEmail());
        assertThat(captor.getValue().getCredentials()).isEqualTo("plain-password");
    }

    @Test
    void login_badCredentials_propagatesAndIssuesNoToken() {
        LoginRequest request = new LoginRequest("acme", "jane@acme.com", "wrong");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }
}