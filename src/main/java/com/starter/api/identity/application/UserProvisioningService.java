package com.starter.api.identity.application;

import com.starter.api.identity.domain.AuthenticatedIdentity;
import com.starter.api.identity.domain.User;
import com.starter.api.identity.domain.UserStatus;
import com.starter.api.identity.infrastructure.JdbcUserIdentityRepository;
import com.starter.api.identity.infrastructure.JdbcUserRepository;
import com.starter.api.shared.audit.AuditRecord;
import com.starter.api.shared.audit.AuditService;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProvisioningService {

    private final JdbcUserRepository users;
    private final JdbcUserIdentityRepository identities;
    private final AuditService audit;

    public UserProvisioningService(
            JdbcUserRepository users, JdbcUserIdentityRepository identities, AuditService audit) {
        this.users = users;
        this.identities = identities;
        this.audit = audit;
    }

    @Transactional
    public User provision(AuthenticatedIdentity authenticated) {
        Instant now = Instant.now();
        long userId = users.insert(UserStatus.ACTIVE, authenticated.claims(), now);
        identities.insert(userId, authenticated.identity(), authenticated.provider(), now);
        audit.record(new AuditRecord(
                userId,
                authenticated.identity().subject(),
                "identity.linked",
                "user",
                Long.toString(userId),
                Map.of("issuer", authenticated.identity().issuer(), "provider", authenticated.provider())));
        return users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Provisioned user could not be reloaded: " + userId));
    }
}
