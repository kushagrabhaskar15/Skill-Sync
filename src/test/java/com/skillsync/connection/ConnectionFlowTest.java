package com.skillsync.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.dto.ConnectionRequest;
import com.skillsync.dto.RegisterRequest;
import com.skillsync.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConnectionFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clean() {
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
    void acceptConnectionCreatesConversationAccess() throws Exception {
        String aliceToken = register("Alice", "alice@example.com");
        String bobToken = register("Bob", "bob@example.com");
        Long bobId = userRepository.findByEmail("bob@example.com").orElseThrow().getId();

        MvcResult connectionResult = mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConnectionRequest(bobId, "Hi Bob"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long connectionId = objectMapper.readTree(connectionResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/v1/connections/" + connectionId + "/accept")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/api/v1/chat/conversations")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].otherParticipant.email").value("bob@example.com"));
    }

    private String register(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(name, email, "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
