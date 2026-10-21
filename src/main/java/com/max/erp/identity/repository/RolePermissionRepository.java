package com.max.erp.identity.repository;

import com.max.erp.identity.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, UUID> {

    @Query("SELECT rp FROM RolePermission rp LEFT JOIN FETCH rp.permission LEFT JOIN FETCH rp.role WHERE rp.role.id = :roleId")
    List<RolePermission> findAllByRoleId(@Param("roleId") UUID roleId);

    List<RolePermission> findAllByPermissionId(UUID permissionId);

    boolean existsByRoleIdAndPermissionId(
            UUID roleId,
            UUID permissionId
    );

    void deleteByRoleIdAndPermissionId(
            UUID roleId,
            UUID permissionId
    );
}