package com.rashid.helpdesk.security;

import com.rashid.helpdesk.support.TestData;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final long ONE_HOUR_MS = 3_600_000L;

    private final JwtService jwtService = new JwtService(TestData.JWT_SECRET, ONE_HOUR_MS);

    @Test
    void generateToken_thenExtract_roundTripsAllClaims() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, tenantId, "AGENT", "agent@acme.com");

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractTenantId(token)).isEqualTo(tenantId);
        assertThat(jwtService.extractRole(token)).isEqualTo("AGENT");
        assertThat(jwtService.extractClaimEmail(token)).isEqualTo("agent@acme.com");
    }

    @Test
    void isTokenValid_freshToken_returnsTrue() {
        String token = jwtService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "ADMIN", "a@acme.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService shortLived = new JwtService(TestData.JWT_SECRET, -1000L);
        String token = shortLived.generateToken(UUID.randomUUID(), UUID.randomUUID(), "ADMIN", "a@acme.com");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_payloadFromAnotherTokenWithOriginalSignature_returnsFalse() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String customerToken = jwtService.generateToken(userId, tenantId, "CUSTOMER", "c@acme.com");
        String adminToken = jwtService.generateToken(userId, tenantId, "ADMIN", "c@acme.com");

        // Privilege escalation attempt: admin payload, but the signature of the customer token.
        String[] customerParts = customerToken.split("\\.");
        String[] adminParts = adminToken.split("\\.");
        String forged = adminParts[0] + "." + adminParts[1] + "." + customerParts[2];

        assertThat(jwtService.isTokenValid(forged)).isFalse();
    }

    @Test
    void isTokenValid_tokenSignedWithDifferentSecret_returnsFalse() {
        JwtService otherIssuer = new JwtService(TestData.OTHER_JWT_SECRET, ONE_HOUR_MS);
        String token = otherIssuer.generateToken(UUID.randomUUID(), UUID.randomUUID(), "ADMIN", "a@acme.com");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "garbage", "garbage.token.value", "a.b.c"})
    void isTokenValid_malformedInput_returnsFalse(String token) {
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void extractUserId_malformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUserId("garbage"))
                .isInstanceOf(JwtException.class);
    }
}