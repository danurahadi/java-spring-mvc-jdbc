package com.starter.api.shared.audit;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuditService {

    private static final String INSERT = """
            INSERT INTO audit_log (occurred_at, actor_user_id, actor_subject, action, target_type, target_id, details)
            VALUES (:occurredAt, :actorUserId, :actorSubject, :action, :targetType, :targetId, :details)
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AuditService(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void record(AuditRecord record) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("occurredAt", OffsetDateTime.now(ZoneOffset.UTC));
        params.put("actorUserId", record.actorUserId());
        params.put("actorSubject", record.actorSubject());
        params.put("action", record.action());
        params.put("targetType", record.targetType());
        params.put("targetId", record.targetId());
        params.put("details", record.details().isEmpty() ? null : objectMapper.writeValueAsString(record.details()));
        jdbc.update(INSERT, params);
    }
}
