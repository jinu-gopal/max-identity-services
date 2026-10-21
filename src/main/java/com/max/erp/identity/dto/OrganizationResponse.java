package com.max.erp.identity.dto;

import com.max.erp.identity.entity.Organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        UUID tenantId,
        String tenantKey,
        UUID parentOrganizationId,
        String parentOrganizationName,
        String organizationKey,
        String name,
        String organizationType,
        String status,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrganizationResponse from(Organization org) {
        if (org == null) {
            return null;
        }

        UUID tenantId = null;
        String tenantKey = null;
        if (org.getTenant() != null) {
            tenantId = org.getTenant().getId();
            try {
                tenantKey = org.getTenant().getTenantKey();
            } catch (Exception ignored) {
                // lazy proxy fallback
            }
        }

        UUID parentId = null;
        String parentName = null;
        if (org.getParentOrganization() != null) {
            try {
                parentId = org.getParentOrganization().getId();
                parentName = org.getParentOrganization().getName();
            } catch (Exception ignored) {
                // lazy proxy fallback
            }
        }

        return new OrganizationResponse(
                org.getId(),
                tenantId,
                tenantKey,
                parentId,
                parentName,
                org.getOrganizationKey(),
                org.getName(),
                org.getOrganizationType(),
                org.getStatus(),
                org.getCreatedAt(),
                org.getUpdatedAt()
        );
    }
}
