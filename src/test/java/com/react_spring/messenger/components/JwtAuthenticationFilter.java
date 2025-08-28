package com.react_spring.messenger.components;

import com.react_spring.messenger.system.jwt.service.JwtService;
import com.react_spring.messenger.system.filter.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;


class JwtAuthenticationFilterTest { //TODO

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        // Clear context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthenticationWhenValidToken() throws ServletException, IOException {
        String token = "valid.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null);
        when(jwtService.getAuthentication(token)).thenReturn(auth);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(auth, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenNoHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenInvalidToken() throws ServletException, IOException {
        String token = "invalid.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreNonBearerHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic something");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
