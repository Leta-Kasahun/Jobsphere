package com.jobsphere.jobsite.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    private final Key signingKey;
    private final long defaultExpirationMs;
    
    public JwtUtils(String secret, long defaultExpirationMs) {
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.defaultExpirationMs = defaultExpirationMs;
    }
    
    public String generateToken(String subject, Map<String, Object> claims, Long customExpirationMs) {
        Date now = new Date();
        long expirationMs = customExpirationMs != null ? customExpirationMs : defaultExpirationMs;
        Date expiry = new Date(now.getTime() + expirationMs);
        
        Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS256);
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
            .compact();
    }
    
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }
}