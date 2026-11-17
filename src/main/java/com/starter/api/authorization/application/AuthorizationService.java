package com.starter.api.authorization.application;

import com.starter.api.authorization.infrastructure.JdbcAuthorizationRepository;
import com.starter.api.shared.security.CurrentUser;
import com.starter.api.shared.security.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private final CurrentUserProvider currentUserProvider;
    private final JdbcAuthorizationRepository repository;

    public AuthorizationService(CurrentUserProvider currentUserProvider, JdbcAuthorizationRepository repository) {
        this.currentUserProvider = currentUserProvider;
        this.repository = repository;
    }

    public boolean hasPermission(Authentication authentication, String permissionCode) {
        if (authentication == null || !authentication.isAuthenticated()
                || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }
        CurrentUser current = currentUserProvider.requireActive(authentication);
        return repository.hasPermission(current.userId(), permissionCode);
    }
}
