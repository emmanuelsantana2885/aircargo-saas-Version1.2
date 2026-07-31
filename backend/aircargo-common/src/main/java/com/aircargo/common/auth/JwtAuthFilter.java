package com.aircargo.common.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.info("JWT token present for {} {} (len={})", method, uri, token.length());
            try {
                if (jwtUtil.isRevoked(token)) {
                    log.info("JWT token REVOKED for {} {}", method, uri);
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(response, "Token revoked");
                    return;
                }
                Claims claims = jwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                log.info("JWT valid for {} {} role={}", method, uri, role);
                if (role == null) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(response, "Invalid token (no role)");
                    return;
                }
                String userId = claims.getSubject();
                String airlineId = claims.get("airlineId", String.class);
                String email = claims.get("email", String.class);
                String fullName = claims.get("fullName", String.class);

                UserPrincipal principal = new UserPrincipal(userId, role, airlineId, email, fullName);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Auth SET for {} {} role={}", method, uri, role);
            } catch (Exception e) {
                log.info("JWT INVALID for {} {}: {}", method, uri, e.getMessage());
                SecurityContextHolder.clearContext();
                writeUnauthorized(response, "Token expired or invalid");
                return;
            }
        } else {
            log.info("NO Bearer token for {} {}", method, uri);
        }
        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        response.getWriter().flush();
    }
}
