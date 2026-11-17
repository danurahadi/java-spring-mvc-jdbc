package com.starter.api.identity.api;

import java.util.List;

public record UserRolesResponse(long userId, List<String> roles) {
}
