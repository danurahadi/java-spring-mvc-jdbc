package com.starter.api.identity.domain;

import java.time.Instant;

public record User(
        long id,
        String externalUserId,
        UserStatus status,
        String email,
        String displayName,
        String contactNumber,
        String pictureUrl,
        String externalRole,
        Instant createdAt,
        Instant updatedAt) {
}
