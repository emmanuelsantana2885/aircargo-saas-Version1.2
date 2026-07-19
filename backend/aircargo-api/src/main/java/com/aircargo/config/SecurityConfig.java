package com.aircargo.config;

import com.aircargo.common.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bi/**").hasAnyAuthority("ADMIN", "SUPER_USER", "BI_USER")
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("READ_ONLY", "WAREHOUSE_ASSISTANT", "OPERATIONS", "TRAFFIC", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyAuthority("WAREHOUSE_ASSISTANT", "OPERATIONS", "TRAFFIC", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyAuthority("WAREHOUSE_ASSISTANT", "OPERATIONS", "TRAFFIC", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyAuthority("ADMIN", "SUPER_USER")
                .requestMatchers("/api/bookings/**").hasAnyAuthority("TRAFFIC", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/receipts/**", "/api/warehouse/**").hasAnyAuthority("WAREHOUSE_ASSISTANT", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/flights/**").hasAnyAuthority("OPERATIONS", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/mawbs/**").hasAnyAuthority("OPERATIONS", "TRAFFIC", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/ulds/**", "/api/uld-awbs/**", "/api/uld-type-config/**").hasAnyAuthority("OPERATIONS", "TRAFFIC", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/load-planning/**").hasAnyAuthority("OPERATIONS", "TRAFFIC", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/cargo/mawbs/**").hasAnyAuthority("OPERATIONS", "TRAFFIC", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/cargo/hawbs/**").hasAnyAuthority("OPERATIONS", "TRAFFIC", "ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.GET, "/api/users/connected").authenticated()
                .requestMatchers("/api/users/**").hasAnyAuthority("ADMIN", "SUPER_USER")
                .requestMatchers("/api/audit-logs/**").hasAnyAuthority("ADMIN", "SUPER_USER")
                .requestMatchers("/api/sites/**").hasAuthority("SUPER_USER")
                .requestMatchers("/api/role-permissions/**").hasAuthority("SUPER_USER")
                .requestMatchers(HttpMethod.GET, "/api/exports/**").hasAnyAuthority("READ_ONLY", "WAREHOUSE_ASSISTANT", "OPERATIONS", "TRAFFIC", "LOAD_PLANNER", "ADMIN", "SUPER_USER")
                .requestMatchers("/api/compliance/**").hasAnyAuthority("ADMIN", "SUPER_USER")
                .requestMatchers("/api/tracking/**").hasAnyAuthority("ADMIN", "SUPER_USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}