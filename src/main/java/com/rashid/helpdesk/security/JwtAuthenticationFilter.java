package com.rashid.helpdesk.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER_NAME);

        // No/invalid header format -> just continue the chain.
        // We don't reject here; SecurityConfig decides per-endpoint whether auth is required.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (jwtService.isTokenValid(token)) {
            UUID userId = jwtService.extractUserId(token);
            UUID tenantId = jwtService.extractTenantId(token);
            String email = jwtService.extractClaimEmail(token);
            String role = jwtService.extractRole(token);

            AuthenticatedUser principal = new AuthenticatedUser(userId, tenantId, email);

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // credentials = null: we're not storing/checking a password here, the token itself was already verified.
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

            // This is the key line: it tells the rest of Spring Security "this request is authenticated as principal, with these authorities".
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // If invalid, we simply don't set an Authentication - request proceeds as anonymous
        // and gets rejected downstream by SecurityConfig if the endpoint requires auth.

        filterChain.doFilter(request, response);
    }
}