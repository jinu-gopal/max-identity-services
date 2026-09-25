package com.max.erp.identity.dto;

import com.max.erp.identity.entity.UserTenantAccess;

import java.util.UUID;

public record UserTenantAccessResponse(
        UUID id,
        UUID userId,
        UUID tenantId,
        String tenantKey,
        String tenantName,
        UUID organizationId,
        String status
) {

    public static UserTenantAccessResponse from(
            UserTenantAccess access) {

        return new UserTenantAccessResponse(
                access.getId(),
                access.getUser().getId(),
                access.getTenant().getId(),
                access.getTenant().getTenantKey(),
                access.getTenant().getName(),
                access.getOrganizationId(),
                access.getStatus()
        );
    }
}