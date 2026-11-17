package com.starter.api.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record AppProperties(@Valid @NotNull Auth0 auth0, @Valid Cors cors, @Valid OpenApi openapi) {

    public AppProperties {
        cors = cors == null ? new Cors(List.of()) : cors;
        openapi = openapi == null ? new OpenApi(null, null) : openapi;
    }

    public record Auth0(
            @NotBlank String issuerUri,
            @NotBlank String jwkSetUri,
            @NotBlank String audience) {
    }

    public record Cors(List<String> allowedOrigins) {

        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        }
    }

    public record OpenApi(Access access, String serverUrl) {

        public OpenApi {
            access = access == null ? Access.AUTHENTICATED : access;
        }

        public enum Access {
            PUBLIC,
            AUTHENTICATED,
            DISABLED
        }
    }
}
