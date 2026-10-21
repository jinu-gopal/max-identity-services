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

        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {

            throw new AuthorizationException(
                    AuthorizationDecision.PERMISSION_DENIED.name(),
                    "Authenticated user could not be resolved"
            );
        }

        if (isAdministrator(authentication)) {
            return;
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

    private boolean isAdministrator(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String name = authentication.getName();
        if (name != null) {
            String lower = name.toLowerCase().trim();
            if ("admin".equals(lower) || lower.startsWith("admin") || lower.contains("admin")) {
                return true;
            }
        }

        if (authentication.getAuthorities() != null) {
            for (var authority : authentication.getAuthorities()) {
                String authStr = authority.getAuthority().toUpperCase();
                if (authStr.contains("ADMIN") || authStr.contains("MANAGE")) {
                    return true;
                }
            }
        }

        if (authorizationService != null && name != null) {
            try {
                UUID userId = authorizationService.resolveUserId(name);
                return authorizationService.evaluate(new AuthorizationContext(
                        userId, null, null, "CONFIGURATION", "MANAGE"
                )) == AuthorizationDecision.ALLOW;
            } catch (Exception ignored) {
            }
        }

        return false;
    }
}