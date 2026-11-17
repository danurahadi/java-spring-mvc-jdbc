package com.starter.api.identity.application;

import com.starter.api.identity.api.UpdateProfileRequest;
import com.starter.api.identity.api.UserResponse;
import com.starter.api.identity.infrastructure.JdbcUserRepository;
import com.starter.api.shared.audit.AuditRecord;
import com.starter.api.shared.audit.AuditService;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final CurrentUserService currentUserService;
    private final JdbcUserRepository users;
    private final AuditService audit;

    public UserProfileService(CurrentUserService currentUserService, JdbcUserRepository users, AuditService audit) {
        this.currentUserService = currentUserService;
        this.users = users;
        this.audit = audit;
    }

    public UserResponse me(Authentication authentication) {
        return UserResponse.from(currentUserService.requireActiveResolved(authentication).user());
    }

    @Transactional
    public UserResponse update(Authentication authentication, UpdateProfileRequest request) {
        ResolvedUser resolved = currentUserService.requireActiveResolved(authentication);
        long userId = resolved.user().id();
        users.updateProfile(userId, request.displayName(), request.contactNumber(), request.email(), Instant.now());
        audit.record(new AuditRecord(
                resolved.current().userId(),
                resolved.current().subject(),
                "user.profile.updated",
                "user",
                Long.toString(userId),
                Map.of()));
        return UserResponse.from(users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Updated user could not be reloaded: " + userId)));
    }
}
