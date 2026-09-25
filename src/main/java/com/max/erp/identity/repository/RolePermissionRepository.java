package com.max.erp.identity.repository;

import com.max.erp.identity.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findAllByRoleId(UUID roleId);

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