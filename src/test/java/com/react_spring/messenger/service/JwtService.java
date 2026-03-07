package com.react_spring.messenger.service;

import com.react_spring.messenger.system.user.model.User;
import com.react_spring.messenger.system.jwt.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest { //TODO

    private static final String TEST_SECRET =
            "test-secret-key-for-testing-only-must-be-at-least-256-bits-long-aaaa";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
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
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String token = Jwts.builder()
                .subject(testUser.getUsername())
                .claim("userId", testUser.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1))
                .signWith(key)
                .compact();

        Thread.sleep(5);

        assertFalse(jwtService.validateToken(token));
    }
}
