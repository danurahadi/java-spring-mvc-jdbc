package com.starter.api.identity.api;

import com.starter.api.identity.domain.User;
import com.starter.api.identity.domain.UserStatus;
import java.time.Instant;

public record UserResponse(
        long id,
        String userId,
        String role,
        UserStatus status,
        String email,
        String displayName,
        String contactNumber,
        String pictureUrl,
        Instant createdAt,
        Instant updatedAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.externalUserId(),
                user.externalRole(),
                user.status(),
                user.email(),
                user.displayName(),
                user.contactNumber(),
                user.pictureUrl(),
                user.createdAt(),
                user.updatedAt());
    }
}
