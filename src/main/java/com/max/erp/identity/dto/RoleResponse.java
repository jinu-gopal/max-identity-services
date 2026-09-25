package com.max.erp.identity.dto;

import com.max.erp.identity.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String roleKey,
        String name,
        String description,
        String status,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt
) {

    public static RoleResponse from(Role role, UUID tenantId) {
        return new RoleResponse(
                role.getId(),
                role.getRoleKey(),
                role.getName(),
                role.getDescription(),
                role.getStatus(),
                tenantId,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}