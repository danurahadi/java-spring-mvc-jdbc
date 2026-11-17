package com.starter.api.identity.domain;

public record ProfileClaims(String email, String displayName, String pictureUrl, String externalRole) {

    public static ProfileClaims empty() {
        return new ProfileClaims(null, null, null, null);
    }

    public boolean isEmpty() {
        return email == null && displayName == null && pictureUrl == null && externalRole == null;
    }
}
