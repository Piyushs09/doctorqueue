package com.doctorqueue.doctorqueue.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "doctorqueue-secret-key-change-this-in-production-very-long";

    private static final long EXPIRATION =
            1000L * 60 * 60 * 24;

    private final SecretKey key =
            Keys.hmacShaKeyFor(
                    SECRET.getBytes(StandardCharsets.UTF_8)
            );

    public String generateToken(
            String email,
            Long userId,
            String role
    ) {

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION
                        )
                )
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {

        return getClaims(token)
                .getSubject();
    }

    public Long extractUserId(String token) {

        Number userId =
                getClaims(token)
                        .get("userId", Number.class);

        return userId.longValue();
    }

    public String extractRole(String token) {

        return getClaims(token)
                .get("role", String.class);
    }

    public boolean isTokenValid(String token) {

        try {

            getClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}