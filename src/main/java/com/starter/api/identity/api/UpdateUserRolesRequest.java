package com.starter.api.identity.api;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateUserRolesRequest(List<@NotBlank String> roles) {

    public UpdateUserRolesRequest {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }
}
