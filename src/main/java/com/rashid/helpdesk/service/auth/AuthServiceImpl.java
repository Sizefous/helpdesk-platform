package com.rashid.helpdesk.service.auth;

import com.rashid.helpdesk.dto.LoginRequest;
import com.rashid.helpdesk.dto.LoginResponse;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.security.JwtService;
import com.rashid.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Pack tenantSlug + email into the single identifier CustomUserDetailsService expects.
        String combinedId = request.tenantSlug() + ":" + request.email();

        // This throws BadCredentialsException (unchecked) if the password is wrong
        // or the user/tenant combination doesn't exist. We let GlobalExceptionHandler deal with it.
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(combinedId, request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        String token = jwtService.generateToken(
                user.getId(),
                user.getTenant().getId(),
                user.getRole().name(),
                user.getEmail()
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getTenant().getId(),
                user.getRole().name(),
                user.getEmail()
        );
    }
}