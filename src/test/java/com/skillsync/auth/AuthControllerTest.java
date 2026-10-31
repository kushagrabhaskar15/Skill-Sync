package com.skillsync.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.dto.LoginRequest;
import com.skillsync.dto.RegisterRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanUsers() {
        entityManager.createNativeQuery("DELETE FROM recommendation_feedback").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM messages").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM conversation_participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM conversations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM recommendations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM connections").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_skills").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_interests").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
    }

    @Test
    void registerAndLoginSucceed() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Alice", "alice@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.userSummary.email").value("alice@example.com"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
