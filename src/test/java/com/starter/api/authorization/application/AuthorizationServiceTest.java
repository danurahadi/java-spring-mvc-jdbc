package com.starter.api.authorization.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.starter.api.authorization.infrastructure.JdbcAuthorizationRepository;
import com.starter.api.shared.security.CurrentUser;
import com.starter.api.shared.security.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private JdbcAuthorizationRepository repository;

    private AuthorizationService service;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(currentUserProvider, repository);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer("https://tenant.test/")
                .subject("auth0|user")
                .audience(List.of("api"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void deniesWhenAuthenticationIsMissing() {
        assertThat(service.hasPermission(null, "users:read")).isFalse();
        verifyNoInteractions(currentUserProvider, repository);
    }

    @Test
    void deniesWhenPermissionCodeIsBlank() {
        assertThat(service.hasPermission(authentication, " ")).isFalse();
        verifyNoInteractions(currentUserProvider, repository);
    }

    @Test
    void delegatesToRepositoryWithResolvedLocalUser() {
        when(currentUserProvider.requireActive(authentication)).thenReturn(new CurrentUser(42L, "issuer", "subject"));
        when(repository.hasPermission(42L, "users:read")).thenReturn(true);

        assertThat(service.hasPermission(authentication, "users:read")).isTrue();
    }

    @Test
    void deniesWhenRepositoryDenies() {
        when(currentUserProvider.requireActive(authentication)).thenReturn(new CurrentUser(42L, "issuer", "subject"));
        when(repository.hasPermission(42L, "users:read")).thenReturn(false);

        assertThat(service.hasPermission(authentication, "users:read")).isFalse();
    }
}
