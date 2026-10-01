package com.rashid.helpdesk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        // Keys.hmacShaKeyFor expects raw bytes long enough for HMAC-SHA256 (>= 256 bits).
        // The secret is stored Base64-encoded in config, so we decode it back to bytes here.
        this.signingKey = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretBase64));
        this.expirationMs = expirationMs;
    }

    /**
     * Builds a signed JWT for the given user.
     * userId and tenantId let every future request identify "who" and "which tenant"
     * without touching the database. role lets us do authorization checks (see SecurityConfig later).
     */
    public String generateToken(UUID userId, UUID tenantId, String role, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())              // "sub" claim - standard field for "who this token is about"
                .claims(Map.of(
                        "tenantId", tenantId.toString(),
                        "role", role,
                        "email", email
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();                               // compact() serializes to the final header.payload.signature string
    }

    /** Extracts the userId (the "sub" claim) from a token. Throws if the token is invalid/expired. */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public UUID extractTenantId(String token) {
        return UUID.fromString(extractClaim(token, claims -> claims.get("tenantId", String.class)));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractClaimEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Covers expired tokens, malformed tokens, and bad signatures alike.
            // We deliberately don't distinguish these to the caller - all mean "not authenticated".
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        // parseSignedClaims verifies the signature using signingKey.
        // If someone tampered with the payload, this throws a SignatureException.
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}