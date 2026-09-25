package com.max.erp.identity.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityAuthorizationGuard {

    private final AuthorizationService authorizationService;

    public SecurityAuthorizationGuard(
            AuthorizationService authorizationService) {

        this.authorizationService = authorizationService;
    }

    public void require(
            Authentication authentication,
            UUID targetTenantId,
            UUID organizationId,
            String moduleKey,
            String actionKey) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AuthorizationException(
                    AuthorizationDecision.PERMISSION_DENIED.name(),
                    "Authentication is required"
            );
        }

        String username = authentication.getName();

        if (username == null || username.isBlank()) {

            throw new AuthorizationException(
                    AuthorizationDecision.PERMISSION_DENIED.name(),
                    "Authenticated user could not be resolved"
            );
        }

        UUID authenticatedUserId =
                authorizationService.resolveUserId(
                        username
                );

        AuthorizationContext context =
                new AuthorizationContext(
                        authenticatedUserId,
                        targetTenantId,
                        organizationId,
                        moduleKey,
                        actionKey
                );

        authorizationService.require(context);
    }
}