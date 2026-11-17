package com.starter.api.identity.api;

import com.starter.api.identity.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeUserStatusRequest(@NotNull UserStatus status) {
}
