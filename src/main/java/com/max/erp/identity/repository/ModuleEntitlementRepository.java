package com.max.erp.identity.repository;

import com.max.erp.identity.entity.ModuleEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuleEntitlementRepository
        extends JpaRepository<ModuleEntitlement, UUID> {

    Optional<ModuleEntitlement>
    findByTenantIdAndModuleId(
            UUID tenantId,
            UUID moduleId
    );

    List<ModuleEntitlement>
    findAllByTenantIdOrderByModuleNameAsc(
            UUID tenantId
    );

    List<ModuleEntitlement>
    findAllByTenantIdAndStatusOrderByModuleNameAsc(
            UUID tenantId,
            String status
    );

    boolean existsByTenantIdAndModuleId(
            UUID tenantId,
            UUID moduleId
    );

    boolean existsByTenantIdAndModuleIdAndStatus(
            UUID tenantId,
            UUID moduleId,
            String status
    );
}