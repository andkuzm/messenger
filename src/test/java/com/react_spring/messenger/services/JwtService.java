package com.react_spring.messenger.services;

import com.react_spring.messenger.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest { //TODO

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void testGetAuthentication() {
        String token = jwtService.generateToken(testUser);

        UsernamePasswordAuthenticationToken authentication = jwtService.getAuthentication(token);

        assertNotNull(authentication);
        assertEquals("testUser", authentication.getPrincipal());
        assertEquals(1L, authentication.getDetails());
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        // Generate token with very short expiration by overriding method
        JwtService shortLivedJwtService = new JwtService() {
            @Override
            public String generateToken(User user) {
                long expirationMillis = 1; // 1 ms
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(user.getUsername())
                        .claim("userId", user.getId())
                        .setIssuedAt(new java.util.Date())
                        .setExpiration(new java.util.Date(System.currentTimeMillis() + expirationMillis))
                        .signWith(this.key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                        .compact();
            }
        };

        String token = shortLivedJwtService.generateToken(testUser);

        // Wait to ensure it's expired
        Thread.sleep(5);

        assertFalse(shortLivedJwtService.validateToken(token));
    }
}
