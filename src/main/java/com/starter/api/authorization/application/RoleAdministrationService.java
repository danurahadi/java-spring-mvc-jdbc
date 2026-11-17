package com.starter.api.authorization.application;

import com.starter.api.authorization.api.CreateRoleRequest;
import com.starter.api.authorization.api.PermissionResponse;
import com.starter.api.authorization.api.RoleResponse;
import com.starter.api.authorization.api.UpdateRoleRequest;
import com.starter.api.authorization.domain.Permission;
import com.starter.api.authorization.domain.Role;
import com.starter.api.authorization.infrastructure.JdbcAuthorizationRepository;
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
public class RoleAdministrationService {

    private static final String PROTECTED_ROLE = "admin";

    private final CurrentUserProvider currentUserProvider;
    private final JdbcAuthorizationRepository repository;
    private final AuditService audit;

    public RoleAdministrationService(
            CurrentUserProvider currentUserProvider, JdbcAuthorizationRepository repository, AuditService audit) {
        this.currentUserProvider = currentUserProvider;
        this.repository = repository;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> permissions() {
        return repository.findPermissions().stream()
                .map(RoleAdministrationService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> roles() {
        Map<Long, List<String>> permissions = repository.findRolePermissions();
        return repository.findRoles().stream()
                .map(role -> toResponse(role, permissions.getOrDefault(role.id(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse role(String code) {
        Role role = repository.findRoleByCode(normalize(code))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Role not found"));
        return toResponse(role, repository.findPermissionCodesForRole(role.id()));
    }

    @Transactional
    public RoleResponse create(Authentication actorAuthentication, CreateRoleRequest request) {
        CurrentUser actor = currentUserProvider.requireActive(actorAuthentication);
        String code = normalize(request.code());
        if (repository.findRoleByCode(code).isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "Role already exists: " + code);
        }
        List<Long> permissionIds = resolvePermissions(request.permissions());
        long roleId = repository.insertRole(code, request.name().trim(), trimToNull(request.description()), Instant.now());
        repository.replaceRolePermissions(roleId, permissionIds);
        audit.record(new AuditRecord(
                actor.userId(), actor.subject(), "role.created", "role", code, Map.of()));
        return role(code);
    }

    @Transactional
    public RoleResponse update(Authentication actorAuthentication, String code, UpdateRoleRequest request) {
        CurrentUser actor = currentUserProvider.requireActive(actorAuthentication);
        Role role = repository.findRoleByCode(normalize(code))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Role not found"));
        List<Long> permissionIds = resolvePermissions(request.permissions());
        repository.updateRole(role.id(), request.name().trim(), trimToNull(request.description()), Instant.now());
        repository.replaceRolePermissions(role.id(), permissionIds);
        audit.record(new AuditRecord(
                actor.userId(), actor.subject(), "role.updated", "role", role.code(), Map.of()));
        return role(role.code());
    }

    @Transactional
    public void delete(Authentication actorAuthentication, String code) {
        CurrentUser actor = currentUserProvider.requireActive(actorAuthentication);
        Role role = repository.findRoleByCode(normalize(code))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Role not found"));
        if (PROTECTED_ROLE.equals(role.code())) {
            throw new ApiException(ErrorCode.CONFLICT, "The built-in administrator role cannot be deleted");
        }
        repository.deleteRole(role.id());
        audit.record(new AuditRecord(
                actor.userId(), actor.subject(), "role.deleted", "role", role.code(), Map.of()));
    }

    private List<Long> resolvePermissions(List<String> codes) {
        Set<String> requested = normalizeAll(codes);
        Map<String, Long> found = repository.findPermissionIdsByCodes(requested);
        if (found.size() != requested.size()) {
            Set<String> missing = new LinkedHashSet<>(requested);
            missing.removeAll(found.keySet());
            throw new ApiException(ErrorCode.NOT_FOUND, "Unknown permissions: " + String.join(",", missing));
        }
        return List.copyOf(found.values());
    }

    private static Set<String> normalizeAll(List<String> codes) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String code : codes) {
            normalized.add(normalize(code));
        }
        return normalized;
    }

    private static String normalize(String code) {
        return code == null ? null : code.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(permission.id(), permission.code(), permission.description());
    }

    private static RoleResponse toResponse(Role role, List<String> permissions) {
        return new RoleResponse(role.id(), role.code(), role.name(), role.description(), permissions);
    }
}
