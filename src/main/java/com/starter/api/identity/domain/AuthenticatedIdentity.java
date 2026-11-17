package com.starter.api.identity.domain;

public record AuthenticatedIdentity(ExternalIdentity identity, String provider, ProfileClaims claims) {
}
