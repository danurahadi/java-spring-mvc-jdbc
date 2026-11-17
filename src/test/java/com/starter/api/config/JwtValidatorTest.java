package com.starter.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtValidatorTest {

    private static final String ISSUER = "https://tenant.test/";
    private static final String AUDIENCE = "api";

    private final OAuth2TokenValidator<Jwt> validator = JwtConfiguration.tokenValidator(ISSUER, AUDIENCE);

    @Test
    void acceptsTokenWithMatchingIssuerAndAudience() {
        assertThat(validator.validate(jwt(ISSUER, AUDIENCE)).hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenWithWrongAudience() {
        assertThat(validator.validate(jwt(ISSUER, "other-api")).hasErrors()).isTrue();
    }

    @Test
    void rejectsTokenWithWrongIssuer() {
        assertThat(validator.validate(jwt("https://evil.test/", AUDIENCE)).hasErrors()).isTrue();
    }

    @Test
    void rejectsExpiredToken() {
        Jwt expired = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(ISSUER)
                .subject("auth0|expired")
                .audience(List.of(AUDIENCE))
                .issuedAt(Instant.now().minusSeconds(600))
                .expiresAt(Instant.now().minusSeconds(300))
                .build();
        assertThat(validator.validate(expired).hasErrors()).isTrue();
    }

    private static Jwt jwt(String issuer, String audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(issuer)
                .subject("auth0|subject")
                .audience(List.of(audience))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
