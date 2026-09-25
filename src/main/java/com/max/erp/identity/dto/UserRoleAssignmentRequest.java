package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRoleAssignmentRequest(

        @NotNull(message = "Role ID is required")
        UUID roleId,

        @NotNull(message = "Tenant ID is required")
        UUID tenantId,

        UUID organizationId
) {
}