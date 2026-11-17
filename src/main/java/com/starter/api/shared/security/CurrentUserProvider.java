package com.starter.api.shared.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;

public interface CurrentUserProvider {

    CurrentUser requireActive(Authentication authentication);

    Optional<CurrentUser> find(Authentication authentication);
}
