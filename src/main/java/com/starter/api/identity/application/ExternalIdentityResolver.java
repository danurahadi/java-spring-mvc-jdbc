package com.starter.api.identity.application;

import com.starter.api.identity.domain.AuthenticatedIdentity;
import org.springframework.security.core.Authentication;

public interface ExternalIdentityResolver {

    AuthenticatedIdentity resolve(Authentication authentication);
}
