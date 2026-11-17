package com.starter.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.starter.api.identity.application.CurrentUserService;
import com.starter.api.identity.application.ResolvedUser;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class ProvisioningConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://tenant.test/";
    private static final String SUBJECT = "auth0|concurrent-user";

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void provisionsExactlyOneLocalUserUnderConcurrency() throws Exception {
        Authentication authentication = authentication(SUBJECT);
        int attempts = 12;

        Set<Long> userIds = new HashSet<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ResolvedUser>> futures = new ArrayList<>();
            for (int index = 0; index < attempts; index++) {
                futures.add(executor.submit(() -> currentUserService.requireActiveResolved(authentication)));
            }
            for (Future<ResolvedUser> future : futures) {
                userIds.add(future.get().user().id());
            }
        }

        assertThat(userIds).hasSize(1);
        Integer identities = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_identities WHERE issuer = ? AND subject = ?",
                Integer.class, ISSUER, SUBJECT);
        assertThat(identities).isEqualTo(1);
    }

    @Test
    void doesNotProvisionInactiveUserTwice() {
        ResolvedUser first = currentUserService.requireActiveResolved(authentication("auth0|second"));
        jdbc.update("UPDATE users SET status = 'SUSPENDED' WHERE id = ?", first.user().id());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> currentUserService.requireActiveResolved(authentication("auth0|second")))
                .isInstanceOf(com.starter.api.shared.error.ApiException.class);

        Integer users = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_identities WHERE issuer = ? AND subject = ?",
                Integer.class, ISSUER, "auth0|second");
        assertThat(users).isEqualTo(1);
    }

    static Authentication authentication(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(ProvisioningConcurrencyIntegrationTest.ISSUER)
                .subject(subject)
                .audience(List.of("api"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
