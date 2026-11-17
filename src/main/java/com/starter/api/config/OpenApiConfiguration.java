package com.starter.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    OpenAPI openAPI(AppProperties properties) {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Application API")
                        .version("v1")
                        .description("Spring MVC and JDBC modular monolith API protected by Auth0 access tokens."))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
        String serverUrl = properties.openapi().serverUrl();
        if (serverUrl != null && !serverUrl.isBlank()) {
            openAPI.servers(List.of(new Server().url(serverUrl)));
        }
        return openAPI;
    }
}
