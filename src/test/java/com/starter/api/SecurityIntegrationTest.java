package com.starter.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

class SecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://tenant.test/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void rejectsAnonymousRequestsWithProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("unauthenticated"));
    }

    @Test
    void returnsProfileForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/me").with(token("auth0|me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value("auth0|me"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void returnsExternalRoleAsReadOnlyProfileData() throws Exception {
        mockMvc.perform(get("/api/v1/me").with(jwt().jwt(builder -> builder
                        .issuer(ISSUER)
                        .subject("auth0|role")
                        .audience(List.of("api"))
                        .claim("role", "Store Manager"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("auth0|role"))
                .andExpect(jsonPath("$.role").value("Store Manager"));
    }

    @Test
    void ignoresExternalRoleForAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").with(jwt().jwt(builder -> builder
                        .issuer(ISSUER)
                        .subject("auth0|fake-admin")
                        .audience(List.of("api"))
                        .claim("role", "admin"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
    }

    @Test
    void updatesProfileWithinRules() throws Exception {
        mockMvc.perform(patch("/api/v1/me")
                        .with(token("auth0|profile"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Jane Doe-1_x","contactNumber":"0812345678","email":"jane@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("auth0|profile"))
                .andExpect(jsonPath("$.displayName").value("Jane Doe-1_x"))
                .andExpect(jsonPath("$.contactNumber").value("0812345678"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void rejectsInvalidProfileFields() throws Exception {
        for (String body : List.of(
                "{\"displayName\":\"Jane!\",\"contactNumber\":null,\"email\":null}",
                "{\"displayName\":null,\"contactNumber\":\"08123456789\",\"email\":null}",
                "{\"displayName\":null,\"contactNumber\":\"08abc\",\"email\":null}",
                "{\"displayName\":null,\"contactNumber\":null,\"email\":\"a-very-long-local-part-that-breaks-the-limit@example.com\"}")) {
            mockMvc.perform(patch("/api/v1/me")
                            .with(token("auth0|invalid-fields"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("validation_failed"))
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }

    @Test
    void forbidsAdministrationWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").with(token("auth0|plain")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
    }

    @Test
    void allowsAdministrationWithAssignedRole() throws Exception {
        String subject = "auth0|administrator";
        mockMvc.perform(get("/api/v1/me").with(token(subject))).andExpect(status().isOk());

        Long userId = jdbc.queryForObject(
                "SELECT user_id FROM user_identities WHERE issuer = ? AND subject = ?",
                Long.class, ISSUER, subject);
        jdbc.update("""
                INSERT INTO user_roles (user_id, role_id, assigned_at)
                SELECT ?, id, UTC_TIMESTAMP(6) FROM roles WHERE code = 'admin'
                """, userId);

        mockMvc.perform(get("/api/v1/admin/users").with(token(subject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void rejectsInvalidProfileUpdate() throws Exception {
        mockMvc.perform(patch("/api/v1/me")
                        .with(token("auth0|validation"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    private static JwtRequestPostProcessor token(String subject) {
        return jwt().jwt(builder -> builder
                .issuer(ISSUER)
                .subject(subject)
                .audience(List.of("api")));
    }
}
