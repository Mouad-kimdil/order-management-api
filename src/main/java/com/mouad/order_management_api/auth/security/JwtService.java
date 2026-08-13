package com.mouad.order_management_api.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(SecurityUser principal) {
        return generateToken(principal, TokenType.ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(SecurityUser principal) {
        return generateToken(principal, TokenType.REFRESH, refreshExpirationMs);
    }

    public String generateToken(SecurityUser principal, TokenType type, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("id", principal.getId().toString())
                .claim("role", principal.getUser().getRole().name())
                .claim("type", type.name())
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equals(parseClaims(token).get("type", String.class));
        }
        catch (Exception ex) {
            return false;
        }
    }

    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }
}