package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserTenantAccessRequest(

        @NotNull(message = "Tenant ID is required")
        UUID tenantId,

        UUID organizationId
) {
}