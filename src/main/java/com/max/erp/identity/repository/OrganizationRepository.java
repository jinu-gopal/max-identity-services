package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationRepository
        extends JpaRepository<Organization, UUID> {

    List<Organization> findAllByTenantIdOrderByNameAsc(
            UUID tenantId
    );

    boolean existsByTenantIdAndOrganizationKey(
            UUID tenantId,
            String organizationKey
    );
}
