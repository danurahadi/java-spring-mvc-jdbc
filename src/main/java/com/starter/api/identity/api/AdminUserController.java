package com.starter.api.identity.api;

import com.starter.api.shared.security.RequiresPermission;
import com.starter.api.authorization.application.UserRoleAssignmentService;
import com.starter.api.identity.application.UserAdministrationService;
import com.starter.api.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Users")
@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserAdministrationService users;
    private final UserRoleAssignmentService roles;

    public AdminUserController(UserAdministrationService users, UserRoleAssignmentService roles) {
        this.users = users;
        this.roles = roles;
    }

    @Operation(summary = "List application users")
    @RequiresPermission("users:read")
    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return users.list(page, size);
    }

    @Operation(summary = "Read an application user")
    @RequiresPermission("users:read")
    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable long userId) {
        return users.get(userId);
    }

    @Operation(summary = "Update an application user profile")
    @RequiresPermission("users:update")
    @PatchMapping("/{userId}")
    public UserResponse update(
            Authentication authentication, @PathVariable long userId, @Valid @RequestBody AdminUpdateUserRequest request) {
        return users.update(authentication, userId, request);
    }

    @Operation(summary = "Change an application user account status")
    @RequiresPermission("users:update")
    @PutMapping("/{userId}/status")
    public UserResponse changeStatus(
            Authentication authentication, @PathVariable long userId,
            @Valid @RequestBody ChangeUserStatusRequest request) {
        return users.changeStatus(authentication, userId, request);
    }

    @Operation(summary = "Read the roles assigned to an application user")
    @RequiresPermission("users:read")
    @GetMapping("/{userId}/roles")
    public UserRolesResponse roles(@PathVariable long userId) {
        return roles.roles(userId);
    }

    @Operation(summary = "Replace the roles assigned to an application user")
    @RequiresPermission("users:assign-roles")
    @PutMapping("/{userId}/roles")
    public UserRolesResponse replaceRoles(
            Authentication authentication, @PathVariable long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return roles.replace(authentication, userId, request);
    }
}
