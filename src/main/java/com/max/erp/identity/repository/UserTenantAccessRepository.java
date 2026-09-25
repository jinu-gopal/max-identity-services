package com.max.erp.identity.repository;

import com.max.erp.identity.entity.UserTenantAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTenantAccessRepository
        extends JpaRepository<UserTenantAccess, UUID> {

    List<UserTenantAccess> findAllByUserId(UUID userId);

    List<UserTenantAccess> findAllByUserIdAndTenantId(
            UUID userId,
            UUID tenantId
    );

    boolean existsByUserIdAndTenantIdAndStatus(
            UUID userId,
            UUID tenantId,
            String status
    );

    boolean existsByUserIdAndTenantIdAndOrganizationId(
            UUID userId,
            UUID tenantId,
            UUID organizationId
    );

    Optional<UserTenantAccess>
    findByUserIdAndTenantIdAndOrganizationId(
            UUID userId,
            UUID tenantId,
            UUID organizationId
    );
}