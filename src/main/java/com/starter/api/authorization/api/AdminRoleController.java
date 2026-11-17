package com.starter.api.authorization.api;

import com.starter.api.authorization.application.RoleAdministrationService;
import com.starter.api.shared.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Admin Roles")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminRoleController {

    private final RoleAdministrationService roles;

    public AdminRoleController(RoleAdministrationService roles) {
        this.roles = roles;
    }

    @Operation(summary = "List permissions")
    @RequiresPermission("roles:read")
    @GetMapping("/permissions")
    public List<PermissionResponse> permissions() {
        return roles.permissions();
    }

    @Operation(summary = "List roles")
    @RequiresPermission("roles:read")
    @GetMapping("/roles")
    public List<RoleResponse> roles() {
        return roles.roles();
    }

    @Operation(summary = "Read a role")
    @RequiresPermission("roles:read")
    @GetMapping("/roles/{code}")
    public RoleResponse role(@PathVariable String code) {
        return roles.role(code);
    }

    @Operation(summary = "Create a role")
    @RequiresPermission("roles:write")
    @PostMapping("/roles")
    public ResponseEntity<RoleResponse> create(
            Authentication authentication, @Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = roles.create(authentication, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(created.code())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a role and its permissions")
    @RequiresPermission("roles:write")
    @PutMapping("/roles/{code}")
    public RoleResponse update(
            Authentication authentication, @PathVariable String code, @Valid @RequestBody UpdateRoleRequest request) {
        return roles.update(authentication, code, request);
    }

    @Operation(summary = "Delete a role")
    @RequiresPermission("roles:write")
    @DeleteMapping("/roles/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable String code) {
        roles.delete(authentication, code);
    }
}
