package com.abcd.hrms.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Enterprise JWT Token Provider for stateless OAuth2/OIDC token generation and claim validation.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${abcd.security.jwt.secret:ABCD-ENTERPRISE-SUPER-SECRET-JWT-SIGNING-KEY-FOR-ABCD-HRMS-PLATFORM-2026-MUST-BE-64-BYTES!}")
    private String jwtSecret;

    @Value("${abcd.security.jwt.expiration-minutes:120}")
    private long expirationMinutes;

    @Value("${abcd.security.jwt.issuer:https://auth.abcd-hrms.corp}")
    private String issuer;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT with full enterprise claims.
     */
    public String generateToken(String username, UUID userId, UUID companyId, UUID legalEntityId,
                                Collection<String> roles, Collection<String> permissions) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("userId", userId != null ? userId.toString() : null)
                .claim("companyId", companyId != null ? companyId.toString() : null)
                .claim("legalEntityId", legalEntityId != null ? legalEntityId.toString() : null)
                .claim("roles", roles != null ? new ArrayList<>(roles) : Collections.emptyList())
                .claim("permissions", permissions != null ? new ArrayList<>(permissions) : Collections.emptyList())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates and parses claims from JWT.
     */
    public Claims parseAndValidateClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid or expired JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
