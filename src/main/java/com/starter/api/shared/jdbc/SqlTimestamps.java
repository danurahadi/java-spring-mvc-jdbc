package com.starter.api.shared.jdbc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class SqlTimestamps {

    private SqlTimestamps() {
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
