package com.starter.api.config;

import com.starter.api.shared.error.SecurityProblemResponder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String[] OPENAPI_PATHS = {"/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"};

    @Bean
    static AnnotationTemplateExpressionDefaults annotationTemplateExpressionDefaults() {
        return new AnnotationTemplateExpressionDefaults();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AppProperties properties, ObjectMapper objectMapper)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll();
                    switch (properties.openapi().access()) {
                        case PUBLIC -> authorize.requestMatchers(OPENAPI_PATHS).permitAll();
                        case AUTHENTICATED -> authorize.requestMatchers(OPENAPI_PATHS).authenticated();
                        case DISABLED -> authorize.requestMatchers(OPENAPI_PATHS).denyAll();
                    }
                    authorize.requestMatchers("/api/**").authenticated();
                    authorize.anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(SecurityProblemResponder.authenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(SecurityProblemResponder.accessDeniedHandler(objectMapper)));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        if (!properties.cors().allowedOrigins().isEmpty()) {
            configuration.setAllowedOrigins(properties.cors().allowedOrigins());
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
            configuration.setMaxAge(3600L);
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
