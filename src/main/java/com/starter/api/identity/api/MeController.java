package com.starter.api.identity.api;

import com.starter.api.identity.application.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Me")
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserProfileService profiles;

    public MeController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @Operation(summary = "Read the authenticated application profile")
    @GetMapping
    public UserResponse me(Authentication authentication) {
        return profiles.me(authentication);
    }

    @Operation(summary = "Update the authenticated application profile")
    @PatchMapping
    public UserResponse update(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return profiles.update(authentication, request);
    }
}
