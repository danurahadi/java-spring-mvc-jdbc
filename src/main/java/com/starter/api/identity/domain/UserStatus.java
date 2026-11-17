package com.starter.api.identity.domain;

public enum UserStatus {
    ACTIVE,
    PENDING,
    SUSPENDED,
    DEACTIVATED;

    public static UserStatus fromDatabase(String value) {
        for (UserStatus status : values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status: " + value);
    }
}
