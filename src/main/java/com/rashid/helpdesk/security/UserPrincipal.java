package com.rashid.helpdesk.security;

import com.rashid.helpdesk.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Adapts our User entity to the shape Spring Security's authentication
 * machinery expects. Only used during login (DaoAuthenticationProvider) -
 * per-request auth uses AuthenticatedUser (built from the JWT) instead.
 */
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    public User getUser() {
        return user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}