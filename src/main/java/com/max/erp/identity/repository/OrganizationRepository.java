package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository
        extends JpaRepository<Organization, UUID> {

    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.parentOrganization LEFT JOIN FETCH o.tenant WHERE o.tenant.id = :tenantId ORDER BY o.name ASC")
    List<Organization> findAllByTenantIdOrderByNameAsc(
            @Param("tenantId") UUID tenantId
    );

    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.parentOrganization LEFT JOIN FETCH o.tenant WHERE o.id = :id AND o.tenant.id = :tenantId")
    Optional<Organization> findByIdAndTenantId(
            @Param("id") UUID id,
            @Param("tenantId") UUID tenantId
    );

    boolean existsByTenantIdAndOrganizationKey(
            UUID tenantId,
            String organizationKey
    );

    boolean existsByIdAndTenantId(
            UUID id,
            UUID tenantId
    );
}
