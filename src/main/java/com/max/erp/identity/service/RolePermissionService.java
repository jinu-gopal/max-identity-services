package com.max.erp.identity.service;

import com.max.erp.identity.entity.Permission;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.entity.RolePermission;
import com.max.erp.identity.repository.PermissionRepository;
import com.max.erp.identity.repository.RolePermissionRepository;
import com.max.erp.identity.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RolePermissionService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository) {

        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository =
                rolePermissionRepository;
    }

    /**
     * Get permissions assigned to a role.
     */
    @Transactional(readOnly = true)
    public List<RolePermission> getPermissions(
            UUID roleId) {

        getRole(roleId);

        return rolePermissionRepository
                .findAllByRoleId(roleId);
    }

    /**
     * Assign permission to role.
     */
    public RolePermission assignPermission(
            UUID roleId,
            UUID permissionId) {

        Role role = getRole(roleId);

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Permission not found"
                                ));

        /*
         * System roles are protected.
         */
        if (Boolean.TRUE.equals(role.isSystem())) {

            throw new IllegalArgumentException(
                    "Permissions of system roles cannot be modified"
            );
        }

        /*
         * Prevent duplicate assignments.
         */
        if (rolePermissionRepository
                .existsByRoleIdAndPermissionId(
                        roleId,
                        permissionId
                )) {

            throw new IllegalArgumentException(
                    "Permission is already assigned to this role"
            );
        }

        /*
         * Tenant roles may only receive permissions that
         * belong to valid MAX modules. Permission records
         * are globally defined, while module entitlement is
         * evaluated when the user actually performs an action.
         */
        RolePermission rolePermission =
                new RolePermission();

        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        return rolePermissionRepository.save(
                rolePermission
        );
    }

    /**
     * Remove permission from role.
     */
    public void removePermission(
            UUID roleId,
            UUID permissionId) {

        Role role = getRole(roleId);

        if (Boolean.TRUE.equals(role.isSystem())) {

            throw new IllegalArgumentException(
                    "Permissions of system roles cannot be modified"
            );
        }

        if (!rolePermissionRepository
                .existsByRoleIdAndPermissionId(
                        roleId,
                        permissionId
                )) {

            throw new IllegalArgumentException(
                    "Permission is not assigned to this role"
            );
        }

        rolePermissionRepository
                .deleteByRoleIdAndPermissionId(
                        roleId,
                        permissionId
                );
    }

    private Role getRole(UUID roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found"
                        ));
    }
}