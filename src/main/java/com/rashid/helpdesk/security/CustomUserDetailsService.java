package com.rashid.helpdesk.security;

import com.rashid.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security's UserDetailsService only accepts a single String identifier,
     * but our login needs both tenantSlug and email to find the right user
     * (email alone is only unique per-tenant, not globally).
     * Convention: pack both into one string as "tenantSlug:email",
     * and unpack it here. The login endpoint (next step) is responsible for
     * building this combined string before calling authenticate().
     */
    @Override
    public UserDetails loadUserByUsername(String combinedId) throws UsernameNotFoundException {
        String[] parts = combinedId.split(":", 2);
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Expected format tenantSlug:email");
        }
        String tenantSlug = parts[0];
        String email = parts[1];

        return userRepository.findByTenantSlugAndEmail(tenantSlug, email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found for tenant '" + tenantSlug + "' with email: " + email));
    }
}