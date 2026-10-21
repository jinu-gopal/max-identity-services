package com.max.erp.identity.dto;

import com.max.erp.identity.entity.Tenant;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String tenantKey,
        String name,
        String status,
        Instant createdAt,
        Instant updatedAt
) {

    public static TenantResponse from(Tenant tenant) {
        if (tenant == null) {
            return null;
        }

        return new TenantResponse(
                tenant.getId(),
                tenant.getTenantKey(),
                tenant.getName(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
