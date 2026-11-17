package com.starter.api.authorization.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRoleRequest(
        @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9:_-]{1,63}") String code,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 255) String description,
        @NotNull List<String> permissions) {

    public CreateRoleRequest {
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }
}
