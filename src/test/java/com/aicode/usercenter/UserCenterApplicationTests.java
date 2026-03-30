package com.aicode.usercenter;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserCenterApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void imdsAiLoginShouldReturnExpectedPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "chenwangshan",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(28800))
                .andExpect(jsonPath("$.username").value("chenwangshan"));
    }

    @Test
    void imdsAiCurrentUserShouldWorkWithBearerToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "chenwangshan",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractJsonValue(loginResponse, "token");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("chenwangshan"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void imdsAiLoginShouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "chenwangshan",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void userManagementPageShouldReturnPaginatedData() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("currentPage", "1")
                        .param("pageSize", "20")
                        .param("username", "chen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordsTotal").isNumber())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void userManagementCrudShouldWorkForImdsAiModule() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        String createdResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "phone": "13900000000",
                                  "email": "%s@example.com",
                                  "password": "abc123"
                                }
                                """.formatted(username, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.phone").value("13900000000"))
                .andExpect(jsonPath("$.email").value("%s@example.com".formatted(username)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = extractJsonLongValue(createdResponse, "id");

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s_updated",
                                  "phone": "13911111111",
                                  "email": "%s_updated@example.com",
                                  "password": "newpass"
                                }
                                """.formatted(username, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("%s_updated".formatted(username)))
                .andExpect(jsonPath("$.phone").value("13911111111"));

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "20")
                        .param("username", username + "_updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordsTotal").value(1))
                .andExpect(jsonPath("$.data[0].username").value("%s_updated".formatted(username)));

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted"));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    private String extractJsonValue(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + fieldName);
        }

        int valueStart = start + pattern.length();
        int valueEnd = json.indexOf('"', valueStart);
        if (valueEnd < 0) {
            throw new IllegalStateException("Invalid JSON for field: " + fieldName);
        }

        return json.substring(valueStart, valueEnd);
    }

    private long extractJsonLongValue(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + fieldName);
        }

        int valueStart = start + pattern.length();
        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }

        if (valueStart == valueEnd) {
            throw new IllegalStateException("Invalid numeric JSON for field: " + fieldName);
        }

        return Long.parseLong(json.substring(valueStart, valueEnd));
    }
}
