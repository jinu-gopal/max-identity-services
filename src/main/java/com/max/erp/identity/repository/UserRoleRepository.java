package com.max.erp.identity.repository;

import com.max.erp.identity.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UUID> {

    List<UserRole> findAllByUserId(UUID userId);

    List<UserRole> findAllByUserIdAndTenantId(
            UUID userId,
            UUID tenantId
    );

    List<UserRole> findAllByUserIdAndTenantIdAndStatus(
            UUID userId,
            UUID tenantId,
            String status
    );

    boolean existsByUserIdAndRoleIdAndTenantIdAndOrganizationId(
            UUID userId,
            UUID roleId,
            UUID tenantId,
            UUID organizationId
    );

    Optional<UserRole>
    findByUserIdAndRoleIdAndTenantIdAndOrganizationId(
            UUID userId,
            UUID roleId,
            UUID tenantId,
            UUID organizationId
    );

    List<UserRole> findAllByRoleId(UUID roleId);
}