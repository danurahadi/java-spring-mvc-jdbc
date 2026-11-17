package com.starter.api.identity.infrastructure;

import com.starter.api.identity.application.UserDirectory;
import com.starter.api.identity.domain.ExternalIdentity;
import com.starter.api.identity.domain.ProfileClaims;
import com.starter.api.identity.domain.User;
import com.starter.api.identity.domain.UserStatus;
import com.starter.api.shared.jdbc.SqlTimestamps;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository implements UserDirectory {

    private static final String PRIMARY_IDENTITY =
            "(SELECT user_id, MIN(subject) AS subject FROM user_identities GROUP BY user_id) i";

    private static final String COLUMNS = """
            u.id,
            i.subject AS external_user_id,
            u.status,
            u.email,
            u.display_name,
            u.contact_number,
            u.picture_url,
            u.external_role,
            u.created_at,
            u.updated_at
            """;

    private static final RowMapper<User> ROW_MAPPER = (rs, _) -> new User(
            rs.getLong("id"),
            rs.getString("external_user_id"),
            UserStatus.fromDatabase(rs.getString("status")),
            rs.getString("email"),
            rs.getString("display_name"),
            rs.getString("contact_number"),
            rs.getString("picture_url"),
            rs.getString("external_role"),
            SqlTimestamps.toInstant(rs.getObject("created_at", OffsetDateTime.class)),
            SqlTimestamps.toInstant(rs.getObject("updated_at", OffsetDateTime.class)));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUserRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> find(long userId) {
        return findById(userId);
    }

    public Optional<User> findById(long id) {
        List<User> users = jdbc.query("""
                SELECT %s
                FROM users u
                         LEFT JOIN %s ON i.user_id = u.id
                WHERE u.id = :id
                """.formatted(COLUMNS, PRIMARY_IDENTITY), Map.of("id", id), ROW_MAPPER);
        return users.stream().findFirst();
    }

    public Optional<User> findByExternalIdentity(ExternalIdentity identity) {
        List<User> users = jdbc.query("""
                SELECT %s
                FROM users u
                         JOIN user_identities i ON i.user_id = u.id
                WHERE i.issuer = :issuer
                  AND i.subject = :subject
                """.formatted(COLUMNS), Map.of("issuer", identity.issuer(), "subject", identity.subject()), ROW_MAPPER);
        return users.stream().findFirst();
    }

    public long insert(UserStatus status, ProfileClaims claims, Instant now) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("email", claims.email())
                .addValue("displayName", claims.displayName())
                .addValue("pictureUrl", claims.pictureUrl())
                .addValue("externalRole", claims.externalRole())
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("""
                INSERT INTO users (status, email, display_name, contact_number, picture_url, external_role,
                                   created_at, updated_at)
                VALUES (:status, :email, :displayName, NULL, :pictureUrl, :externalRole, :now, :now)
                """, params, keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("users insert did not return a generated id");
        }
        return key.longValue();
    }

    public void updateProfile(long id, String displayName, String contactNumber, String email, Instant now) {
        jdbc.update("""
                UPDATE users
                SET display_name   = :displayName,
                    contact_number = :contactNumber,
                    email          = :email,
                    updated_at     = :now
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("displayName", displayName)
                .addValue("contactNumber", contactNumber)
                .addValue("email", email)
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC)));
    }

    public void updateStatus(long id, UserStatus status, Instant now) {
        jdbc.update("""
                UPDATE users
                SET status     = :status,
                    updated_at = :now
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.name())
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC)));
    }

    public List<User> findPage(int limit, int offset) {
        return jdbc.query("""
                SELECT %s
                FROM users u
                         LEFT JOIN %s ON i.user_id = u.id
                ORDER BY u.id ASC
                LIMIT :limit OFFSET :offset
                """.formatted(COLUMNS, PRIMARY_IDENTITY), Map.of("limit", limit, "offset", offset), ROW_MAPPER);
    }

    public long count() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM users", Map.of(), Long.class);
        return total == null ? 0L : total;
    }
}
