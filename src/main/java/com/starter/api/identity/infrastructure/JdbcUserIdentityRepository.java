package com.starter.api.identity.infrastructure;

import com.starter.api.identity.domain.ExternalIdentity;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserIdentityRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUserIdentityRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(long userId, ExternalIdentity identity, String provider, Instant now) {
        jdbc.update("""
                INSERT INTO user_identities (user_id, issuer, subject, provider, created_at)
                VALUES (:userId, :issuer, :subject, :provider, :now)
                """, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("issuer", identity.issuer())
                .addValue("subject", identity.subject())
                .addValue("provider", provider)
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC)));
    }
}
