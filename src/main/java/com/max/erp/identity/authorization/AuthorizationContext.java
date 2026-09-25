package com.max.erp.identity.authorization;

import java.util.UUID;

public record AuthorizationContext(

        UUID userId,

        UUID tenantId,

        UUID organizationId,

        String moduleKey,

        String actionKey
) {
}