package com.starter.api.shared.audit;

import java.util.Map;

public record AuditRecord(
        Long actorUserId,
        String actorSubject,
        String action,
        String targetType,
        String targetId,
        Map<String, String> details) {

    public AuditRecord {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
