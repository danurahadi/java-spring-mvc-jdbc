package com.starter.api.identity.infrastructure;

import com.starter.api.identity.application.ExternalIdentityResolver;
import com.starter.api.identity.domain.AuthenticatedIdentity;
import com.starter.api.identity.domain.ExternalIdentity;
import com.starter.api.identity.domain.ProfileClaims;
import com.starter.api.shared.error.ApiException;
import com.starter.api.shared.error.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class Auth0ExternalIdentityResolver implements ExternalIdentityResolver {

    private static final String PROVIDER = "auth0";

    @Override
    public AuthenticatedIdentity resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "A bearer access token is required");
        }
        String issuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
        String subject = jwt.getSubject();
        if (issuer == null || issuer.isBlank() || subject == null || subject.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Access token is missing an issuer or subject");
        }
        return new AuthenticatedIdentity(new ExternalIdentity(issuer, subject), PROVIDER, claims(jwt));
    }

    private ProfileClaims claims(Jwt jwt) {
        return new ProfileClaims(
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("picture"),
                jwt.getClaimAsString("role"));
    }
}
