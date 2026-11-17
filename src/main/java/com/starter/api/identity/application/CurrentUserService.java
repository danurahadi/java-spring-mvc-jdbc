package com.starter.api.identity.application;

import com.starter.api.identity.domain.AuthenticatedIdentity;
import com.starter.api.identity.domain.ExternalIdentity;
import com.starter.api.identity.domain.User;
import com.starter.api.identity.domain.UserStatus;
import com.starter.api.identity.infrastructure.JdbcUserRepository;
import com.starter.api.shared.error.ApiException;
import com.starter.api.shared.error.ErrorCode;
import com.starter.api.shared.security.CurrentUser;
import com.starter.api.shared.security.CurrentUserProvider;
import java.util.Optional;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService implements CurrentUserProvider {

    private final ExternalIdentityResolver resolver;
    private final JdbcUserRepository users;
    private final UserProvisioningService provisioning;

    public CurrentUserService(
            ExternalIdentityResolver resolver, JdbcUserRepository users, UserProvisioningService provisioning) {
        this.resolver = resolver;
        this.users = users;
        this.provisioning = provisioning;
    }

    public ResolvedUser requireActiveResolved(Authentication authentication) {
        AuthenticatedIdentity identity = resolver.resolve(authentication);
        ExternalIdentity external = identity.identity();
        User user = users.findByExternalIdentity(external).orElseGet(() -> provisionOrReload(identity));
        if (user.status() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_INACTIVE, "The application account is not active");
        }
        return new ResolvedUser(new CurrentUser(user.id(), external.issuer(), external.subject()), user);
    }

    @Override
    public CurrentUser requireActive(Authentication authentication) {
        return requireActiveResolved(authentication).current();
    }

    @Override
    public Optional<CurrentUser> find(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt)) {
            return Optional.empty();
        }
        AuthenticatedIdentity identity = resolver.resolve(authentication);
        ExternalIdentity external = identity.identity();
        return users.findByExternalIdentity(external)
                .filter(user -> user.status() == UserStatus.ACTIVE)
                .map(user -> new CurrentUser(user.id(), external.issuer(), external.subject()));
    }

    private User provisionOrReload(AuthenticatedIdentity identity) {
        try {
            return provisioning.provision(identity);
        } catch (DuplicateKeyException | ConcurrencyFailureException race) {
            return users.findByExternalIdentity(identity.identity())
                    .orElseThrow(() -> new IllegalStateException(
                            "Concurrent provisioning did not produce a local user", race));
        }
    }
}
