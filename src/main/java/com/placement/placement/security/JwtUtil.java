package com.placement.placement.security;

import com.placement.placement.entity.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    // FIX: was 1000 * 60 * 2 (2 minutes) even though comment said 5 minutes.
    // This was causing tokens to expire almost immediately -> random logouts.
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Generate token
    public String generateAccessToken(Admin admin) {
        return Jwts.builder()
                .setSubject(admin.getUsername())
                .claim("userId", admin.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSecretKey())
                .compact();
    }

    // Extract username
    public String getUsernameFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token, Admin admin) {
        String username = getUsernameFromToken(token);
        return username.equals(admin.getUsername()) && !isTokenExpired(token);
    }

    // ---------- helper methods ----------

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        // FIX: was comparing against (now + 2 minutes) instead of just "now",
        // which made every token look expired almost instantly.
        return getAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
}