package com.starter.api.authorization.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 255) String description,
        @NotNull List<String> permissions) {

    public UpdateRoleRequest {
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }
}
