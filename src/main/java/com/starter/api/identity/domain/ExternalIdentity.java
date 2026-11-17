package com.starter.api.identity.domain;

public record ExternalIdentity(String issuer, String subject) {

    public ExternalIdentity {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
    }
}
