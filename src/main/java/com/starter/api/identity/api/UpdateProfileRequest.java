package com.starter.api.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50)
        @Pattern(regexp = "^[\\p{L}\\p{N} _-]*$")
        String displayName,

        @Size(max = 10)
        @Pattern(regexp = "^\\d*$")
        String contactNumber,

        @Email @Size(max = 50)
        String email) {
}
