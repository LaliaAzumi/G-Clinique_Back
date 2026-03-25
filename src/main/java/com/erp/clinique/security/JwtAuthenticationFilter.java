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

        // Ne pas filtrer les requêtes vers /login et /api/**
        String path = request.getRequestURI();
        if (path.equals("/login") || path.startsWith("/api/") || path.startsWith("/css/") || 
            path.startsWith("/js/") || path.startsWith("/images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait le token du cookie ou du header
        String token = extractToken(request);

        if (token != null) {
            // Valide le token via FastAPI
            Map<String, Object> tokenData = fastApiAuthService.validateToken(token);

            if (tokenData != null) {
                // Crée l'authentification Spring Security
                String username = (String) tokenData.get("username");
                String role = (String) tokenData.get("role");

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Token invalide, redirige vers login
                response.sendRedirect("/login");
                return;
            }
        } else {
            // Pas de token, redirige vers login
            response.sendRedirect("/login");
            return;
        }

        filterChain.doFilter(request, response);
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
