package com.starter.api.authorization.application;

import com.starter.api.authorization.infrastructure.JdbcAuthorizationRepository;
import com.starter.api.identity.api.UpdateUserRolesRequest;
import com.starter.api.identity.api.UserRolesResponse;
import com.starter.api.identity.application.UserDirectory;
import com.starter.api.shared.audit.AuditRecord;
import com.starter.api.shared.audit.AuditService;
import com.starter.api.shared.error.ApiException;
import com.starter.api.shared.error.ErrorCode;
import com.starter.api.shared.security.CurrentUser;
import com.starter.api.shared.security.CurrentUserProvider;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleAssignmentService {

    private final CurrentUserProvider currentUserProvider;
    private final UserDirectory directory;
    private final JdbcAuthorizationRepository repository;
    private final AuditService audit;

    public UserRoleAssignmentService(
            CurrentUserProvider currentUserProvider,
            UserDirectory directory,
            JdbcAuthorizationRepository repository,
            AuditService audit) {
        this.currentUserProvider = currentUserProvider;
        this.directory = directory;
        this.repository = repository;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public UserRolesResponse roles(long userId) {
        requireUser(userId);
        return new UserRolesResponse(userId, repository.findRoleCodes(userId));
    }

    @Transactional
    public UserRolesResponse replace(Authentication actorAuthentication, long userId, UpdateUserRolesRequest request) {
        CurrentUser actor = currentUserProvider.requireActive(actorAuthentication);
        if (userId == actor.userId()) {
            throw new ApiException(ErrorCode.CONFLICT, "Administrators cannot change their own roles");
        }
        requireUser(userId);
        Set<String> requested = normalize(request.roles());
        Map<String, Long> found = repository.findRoleIdsByCodes(requested);
        if (found.size() != requested.size()) {
            Set<String> missing = new LinkedHashSet<>(requested);
            missing.removeAll(found.keySet());
            throw new ApiException(ErrorCode.NOT_FOUND, "Unknown roles: " + String.join(",", missing));
        }
        repository.replaceUserRoles(userId, List.copyOf(found.values()), actor.userId(), Instant.now());
        audit.record(new AuditRecord(
                actor.userId(),
                actor.subject(),
                "user.roles.replaced",
                "user",
                Long.toString(userId),
                Map.of("roles", String.join(",", requested))));
        return new UserRolesResponse(userId, repository.findRoleCodes(userId));
    }

    private void requireUser(long userId) {
        if (directory.find(userId).isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "User not found");
        }
    }

    private static Set<String> normalize(List<String> codes) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String code : codes) {
            normalized.add(code.trim().toLowerCase(Locale.ROOT));
        }
        return normalized;
    }
}
