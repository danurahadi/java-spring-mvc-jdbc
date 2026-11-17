package com.starter.api.authorization.api;

import java.util.List;

public record RoleResponse(long id, String code, String name, String description, List<String> permissions) {
}
