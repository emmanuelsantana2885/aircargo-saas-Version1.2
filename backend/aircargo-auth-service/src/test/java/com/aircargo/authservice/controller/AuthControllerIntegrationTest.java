package com.aircargo.authservice.controller;

import com.aircargo.authservice.entity.AppUser;
import com.aircargo.authservice.entity.UserRole;
import com.aircargo.authservice.repository.AppUserRepository;
import com.aircargo.common.entity.Airline;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Airline airline;

    @BeforeEach
    void setUp() {
        airline = Airline.builder()
                .code("TST")
                .name("Test Airline")
                .iataCode("TT")
                .country("DO")
                .isActive(true)
                .build();
        entityManager.persist(airline);
        entityManager.flush();
    }

    private AppUser user(String email, UserRole role) {
        return AppUser.builder()
                .email(email)
                .fullName("Test User")
                .role(role)
                .airline(airline)
                .build();
    }

    @Test
    void login_validUser_returnsToken() throws Exception {
        userRepository.save(user("test@aircargo.com", UserRole.OPERATIONS));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@aircargo.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("test@aircargo.com"))
                .andExpect(jsonPath("$.role").value("OPERATIONS"));
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@aircargo.com\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_inactiveUser_returns403() throws Exception {
        userRepository.save(user("inactive@aircargo.com", UserRole.OPERATIONS));
        AppUser inactive = userRepository.findByEmail("inactive@aircargo.com").orElseThrow();
        inactive.setIsActive(false);
        userRepository.save(inactive);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inactive@aircargo.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_missingPasswordWhenHashSet_returns428() throws Exception {
        userRepository.save(user("pw@aircargo.com", UserRole.ADMIN));
        AppUser pwUser = userRepository.findByEmail("pw@aircargo.com").orElseThrow();
        pwUser.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUV");
        userRepository.save(pwUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pw@aircargo.com\"}"))
                .andExpect(status().isPreconditionRequired());
    }
}

