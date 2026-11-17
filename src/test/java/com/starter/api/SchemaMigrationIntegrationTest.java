package com.starter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

class SchemaMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void appliesEveryMigrationTable() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()", String.class);
        assertThat(tables).contains(
                "flyway_schema_history", "users", "user_identities", "roles",
                "permissions", "user_roles", "role_permissions", "audit_log");
    }

    @Test
    void seedsPermissionsAndAdminRole() {
        Integer permissions = jdbc.queryForObject("SELECT COUNT(*) FROM permissions", Integer.class);
        assertThat(permissions).isEqualTo(5);

        Long adminRoleId = jdbc.queryForObject("SELECT id FROM roles WHERE code = 'admin'", Long.class);
        Integer adminPermissions = jdbc.queryForObject(
                "SELECT COUNT(*) FROM role_permissions WHERE role_id = ?", Integer.class, adminRoleId);
        assertThat(adminPermissions).isEqualTo(5);
    }

    @Test
    void appliesUserProfileColumnRules() {
        assertThat(columnLength("display_name")).isEqualTo(50);
        assertThat(columnLength("email")).isEqualTo(50);
        assertThat(columnLength("contact_number")).isEqualTo(10);
        assertThat(columnLength("external_role")).isEqualTo(50);
    }

    private Long columnLength(String column) {
        return jdbc.queryForObject("""
                SELECT character_maximum_length
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Long.class, "users", column);
    }

    @Test
    void enforcesCaseSensitiveExternalIdentityUniqueness() {
        long userId = insertUser();
        insertIdentity(userId, "https://Issuer.test/", "Subject");

        assertThatThrownBy(() -> insertIdentity(userId, "https://Issuer.test/", "Subject"))
                .isInstanceOf(DuplicateKeyException.class);

        insertIdentity(userId, "https://issuer.test/", "subject");
        Integer identities = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_identities WHERE user_id = ?", Integer.class, userId);
        assertThat(identities).isEqualTo(2);
    }

    private long insertUser() {
        jdbc.update("""
                INSERT INTO users (status, email, display_name, picture_url, created_at, updated_at)
                VALUES ('ACTIVE', NULL, NULL, NULL, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
                """);
        return jdbc.queryForObject("SELECT MAX(id) FROM users", Long.class);
    }

    private void insertIdentity(long userId, String issuer, String subject) {
        jdbc.update("""
                INSERT INTO user_identities (user_id, issuer, subject, provider, created_at)
                VALUES (?, ?, ?, 'auth0', UTC_TIMESTAMP(6))
                """, userId, issuer, subject);
    }
}
