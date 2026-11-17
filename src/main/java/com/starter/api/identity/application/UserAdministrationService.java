package com.starter.api.identity.application;

import com.starter.api.identity.api.AdminUpdateUserRequest;
import com.starter.api.identity.api.ChangeUserStatusRequest;
import com.starter.api.identity.api.UserResponse;
import com.starter.api.identity.domain.User;
import com.starter.api.identity.domain.UserStatus;
import com.starter.api.identity.infrastructure.JdbcUserRepository;
import com.starter.api.shared.api.PageResponse;
import com.starter.api.shared.audit.AuditRecord;
import com.starter.api.shared.audit.AuditService;
import com.starter.api.shared.error.ApiException;
import com.starter.api.shared.error.ErrorCode;
import com.starter.api.shared.security.CurrentUser;
import com.starter.api.shared.security.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {

    private final CurrentUserProvider currentUserProvider;
    private final JdbcUserRepository users;
    private final AuditService audit;

    public UserAdministrationService(
            CurrentUserProvider currentUserProvider, JdbcUserRepository users, AuditService audit) {
        this.currentUserProvider = currentUserProvider;
        this.users = users;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(int page, int size) {
        List<UserResponse> items = users.findPage(size, page * size).stream()
                .map(UserResponse::from)
                .toList();
        return PageResponse.of(items, page, size, users.count());
    }

    @Transactional(readOnly = true)
    public UserResponse get(long userId) {
        return UserResponse.from(requireUser(userId));
    }

    @Transactional
    public UserResponse update(Authentication actorAuthentication, long userId, AdminUpdateUserRequest request) {
        CurrentUser actor = currentUserProvider.requireActive(actorAuthentication);
        requireUser(userId);
        users.updateProfile(userId, request.displayName(), request.contactNumber(), request.email(), Instant.now());
        audit.record(new AuditRecord(
                actor.userId(), actor.subject(), "user.profile.updated", "user", Long.toString(userId), Map.of()));
        return UserResponse.from(requireUser(userId));
    }

    @Transactional
    public UserResponse changeStatus(
            Authentication actorAuthentication, long userId, ChangeUserStatusRequest request) {
        CurrentUser actor = currentUserProvider.requireActive(actorAuthentication);
        UserStatus status = request.status();
        if (userId == actor.userId() && status != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.CONFLICT, "Administrators cannot deactivate their own account");
        }
        requireUser(userId);
        users.updateStatus(userId, status, Instant.now());
        audit.record(new AuditRecord(
                actor.userId(),
                actor.subject(),
                "user.status.changed",
                "user",
                Long.toString(userId),
                Map.of("status", status.name())));
        return UserResponse.from(requireUser(userId));
    }

    private User requireUser(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
    }
}
