package com.BussinesCardApp.demo.user.authentication.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtGenerator {

    private final Key secretKey;
    private final Duration expiry;

    // ========= constructor injectat din .yml =========
    public JwtGenerator(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration}") Duration expiry) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiry    = expiry;
    }

    public String generateToken(String userId, String role, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expiry)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }
}
