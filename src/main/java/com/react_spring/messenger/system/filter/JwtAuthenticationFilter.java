package com.react_spring.messenger.system.filter;

import com.react_spring.messenger.system.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {//TODO

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && jwtService.validateToken(token)) {
            UsernamePasswordAuthenticationToken auth = jwtService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
