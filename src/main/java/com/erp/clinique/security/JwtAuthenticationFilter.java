package com.erp.clinique.security;

import com.erp.clinique.service.FastApiAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Filtre JWT qui valide les tokens via FastAPI
 * Extrait le token du cookie ou du header Authorization
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private FastApiAuthService fastApiAuthService;

    @Override

    protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();

    // 1. Laisser passer les routes publiques sans vérifier le token
    if (path.equals("/login") || path.startsWith("/api/") || path.startsWith("/css/") || 
        path.startsWith("/js/") || path.startsWith("/images/")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = extractToken(request);

    if (token != null) {
        Map<String, Object> tokenData = fastApiAuthService.validateToken(token);

        if (tokenData != null) {
            String username = (String) tokenData.get("username");
            String role = (String) tokenData.get("role");

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
            );

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response); // Continuer normalement
        } else {
            handleUnauthenticated(request, response);
        }
    } else {
        handleUnauthenticated(request, response);
    }
}

/**
 * Gère l'échec d'authentification sans casser les appels API
 */
private void handleUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String path = request.getRequestURI();
    
    // Si c'est une API, on renvoie juste 401 Unauthorized
    if (path.startsWith("/api/")) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized - Token invalid or missing\"}");
    } else {
        // Si c'est une page HTML, on redirige vers le login
        response.sendRedirect("/login");
    }
}

    /**
     * Extrait le token JWT du cookie "jwt_token" ou du header Authorization
     */
    private String extractToken(HttpServletRequest request) {
        // Essayer d'abord le cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Sinon essayer le header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
