package com.aircargo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class CacheHeadersFilter extends OncePerRequestFilter {

    private static final Set<String> STATIC_PATHS = Set.of(
            "/api/airlines",
            "/api/uld-types"
    );

    private static final Set<String> DYNAMIC_PATHS = Set.of(
            "/api/flights",
            "/api/bookings",
            "/api/mawbs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (request.getMethod().equalsIgnoreCase("GET")) {
            if (isUnderPath(path, STATIC_PATHS)) {
                response.setHeader("Cache-Control", "max-age=300");
            } else if (isUnderPath(path, DYNAMIC_PATHS)) {
                response.setHeader("Cache-Control", "no-cache, must-revalidate");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isUnderPath(String path, Set<String> prefixes) {
        for (String prefix : prefixes) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }
}
