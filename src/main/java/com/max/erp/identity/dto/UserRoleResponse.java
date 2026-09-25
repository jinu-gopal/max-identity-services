package com.max.erp.identity.dto;

import com.max.erp.identity.entity.UserRole;

import java.util.UUID;

public record UserRoleResponse(
        UUID id,
        UUID userId,
        UUID roleId,
        String roleKey,
        String roleName,
        UUID tenantId,
        UUID organizationId,
        String status
) {

    public static UserRoleResponse from(UserRole userRole) {

        return new UserRoleResponse(
                userRole.getId(),
                userRole.getUser().getId(),
                userRole.getRole().getId(),
                userRole.getRole().getRoleKey(),
                userRole.getRole().getName(),
                userRole.getTenant().getId(),
                userRole.getOrganizationId(),
                userRole.getStatus()
        );
    }
}