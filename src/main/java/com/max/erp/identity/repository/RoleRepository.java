package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    List<Role> findAllByTenantIsNullOrderByNameAsc();

    List<Role> findAllByTenantIdOrTenantIsNullOrderByNameAsc(
            UUID tenantId
    );

    boolean existsByTenantIdAndRoleKey(
            UUID tenantId,
            String roleKey
    );

    boolean existsByRoleKeyAndTenantIsNull(
            String roleKey
    );
}