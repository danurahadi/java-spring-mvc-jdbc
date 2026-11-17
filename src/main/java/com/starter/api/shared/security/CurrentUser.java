package com.starter.api.shared.security;

public record CurrentUser(long userId, String issuer, String subject) {
}
