package com.starter.api.authorization.infrastructure;

import com.starter.api.authorization.domain.Permission;
import com.starter.api.authorization.domain.Role;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class JdbcAuthorizationRepository {

    private static final RowMapper<Role> ROLE_MAPPER = (rs, _) -> new Role(
            rs.getLong("id"), rs.getString("code"), rs.getString("name"), rs.getString("description"));

    private static final RowMapper<Permission> PERMISSION_MAPPER = (rs, _) -> new Permission(
            rs.getLong("id"), rs.getString("code"), rs.getString("description"));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAuthorizationRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean hasPermission(long userId, String permissionCode) {
        Boolean exists = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM user_roles ur
                             JOIN role_permissions rp ON rp.role_id = ur.role_id
                             JOIN permissions p ON p.id = rp.permission_id
                    WHERE ur.user_id = :userId
                      AND p.code = :code
                )
                """, Map.of("userId", userId, "code", permissionCode), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    public List<String> findRoleCodes(long userId) {
        return jdbc.queryForList("""
                SELECT r.code
                FROM user_roles ur
                         JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = :userId
                ORDER BY r.code
                """, Map.of("userId", userId), String.class);
    }

    public List<Role> findRoles() {
        return jdbc.query("SELECT id, code, name, description FROM roles ORDER BY code", Map.of(), ROLE_MAPPER);
    }

    public Optional<Role> findRoleByCode(String code) {
        return jdbc.query("SELECT id, code, name, description FROM roles WHERE code = :code",
                Map.of("code", code), ROLE_MAPPER).stream().findFirst();
    }

    public List<Permission> findPermissions() {
        return jdbc.query("SELECT id, code, description FROM permissions ORDER BY code", Map.of(), PERMISSION_MAPPER);
    }

    public Map<Long, List<String>> findRolePermissions() {
        Map<Long, List<String>> result = new LinkedHashMap<>();
        jdbc.query("""
                SELECT rp.role_id, p.code
                FROM role_permissions rp
                         JOIN permissions p ON p.id = rp.permission_id
                ORDER BY rp.role_id, p.code
                """, Map.of(), rs -> {
            result.computeIfAbsent(rs.getLong("role_id"), _ -> new java.util.ArrayList<>())
                    .add(rs.getString("code"));
        });
        return result;
    }

    public List<String> findPermissionCodesForRole(long roleId) {
        return jdbc.queryForList("""
                SELECT p.code
                FROM role_permissions rp
                         JOIN permissions p ON p.id = rp.permission_id
                WHERE rp.role_id = :roleId
                ORDER BY p.code
                """, Map.of("roleId", roleId), String.class);
    }

    public Map<String, Long> findPermissionIdsByCodes(Collection<String> codes) {
        if (codes.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> result = new HashMap<>();
        jdbc.query("SELECT id, code FROM permissions WHERE code IN (:codes)",
                Map.of("codes", codes), rs -> {
                    result.put(rs.getString("code"), rs.getLong("id"));
                });
        return result;
    }

    public Map<String, Long> findRoleIdsByCodes(Collection<String> codes) {
        if (codes.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> result = new HashMap<>();
        jdbc.query("SELECT id, code FROM roles WHERE code IN (:codes)",
                Map.of("codes", codes), rs -> {
                    result.put(rs.getString("code"), rs.getLong("id"));
                });
        return result;
    }

    public long insertRole(String code, String name, String description, Instant now) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("""
                INSERT INTO roles (code, name, description, created_at, updated_at)
                VALUES (:code, :name, :description, :now, :now)
                """, new MapSqlParameterSource()
                .addValue("code", code)
                .addValue("name", name)
                .addValue("description", description)
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC)),
                keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("roles insert did not return a generated id");
        }
        return key.longValue();
    }

    public void updateRole(long roleId, String name, String description, Instant now) {
        jdbc.update("""
                UPDATE roles
                SET name = :name, description = :description, updated_at = :now
                WHERE id = :roleId
                """, new MapSqlParameterSource()
                .addValue("roleId", roleId)
                .addValue("name", name)
                .addValue("description", description)
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC)));
    }

    public void deleteRole(long roleId) {
        jdbc.update("DELETE FROM role_permissions WHERE role_id = :roleId", Map.of("roleId", roleId));
        jdbc.update("DELETE FROM user_roles WHERE role_id = :roleId", Map.of("roleId", roleId));
        jdbc.update("DELETE FROM roles WHERE id = :roleId", Map.of("roleId", roleId));
    }

    public void replaceRolePermissions(long roleId, List<Long> permissionIds) {
        jdbc.update("DELETE FROM role_permissions WHERE role_id = :roleId", Map.of("roleId", roleId));
        if (!permissionIds.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permissionId)",
                    permissionIds.stream()
                            .map(permissionId -> new MapSqlParameterSource()
                                    .addValue("roleId", roleId)
                                    .addValue("permissionId", permissionId))
                            .toArray(SqlParameterSource[]::new));
        }
    }

    public void replaceUserRoles(long userId, List<Long> roleIds, Long assignedBy, Instant now) {
        jdbc.update("DELETE FROM user_roles WHERE user_id = :userId", Map.of("userId", userId));
        if (!roleIds.isEmpty()) {
            OffsetDateTime timestamp = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
            jdbc.batchUpdate("""
                    INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by)
                    VALUES (:userId, :roleId, :assignedAt, :assignedBy)
                    """, roleIds.stream()
                    .map(roleId -> new MapSqlParameterSource()
                            .addValue("userId", userId)
                            .addValue("roleId", roleId)
                            .addValue("assignedAt", timestamp)
                            .addValue("assignedBy", assignedBy))
                    .toArray(SqlParameterSource[]::new));
        }
    }
}
