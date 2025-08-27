package com.react_spring.messenger.services;

import com.react_spring.messenger.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtService { //TODO
    private final String secret = "qwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwertyqwerty";
    final Key key = Keys.hmacShaKeyFor(secret.getBytes());

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        Long userId = claims.get("userId", Long.class);  // extract userId

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, List.of());
        auth.setDetails(userId);  // store userId in authentication details
        return auth;
    }

    public String generateToken(User user) {
        long expirationMillis = 1000 * 60 * 60 * 2;
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
