package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreatePermissionRequest;
import com.max.erp.identity.dto.UpdatePermissionRequest;
import com.max.erp.identity.entity.Permission;
import com.max.erp.identity.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(
            PermissionRepository permissionRepository) {

        this.permissionRepository = permissionRepository;
    }

    /**
     * Create a permission.
     */
    public Permission createPermission(
            CreatePermissionRequest request) {

        if (permissionRepository
                .existsByPermissionKey(
                        request.permissionKey())) {

            throw new IllegalArgumentException(
                    "Permission key already exists"
            );
        }

        Permission permission = new Permission();

        permission.setPermissionKey(
                request.permissionKey()
        );

        permission.setName(
                request.name()
        );

        permission.setDescription(
                request.description()
        );

        permission.setModuleKey(
                request.moduleKey()
        );

        permission.setActionKey(
                request.actionKey()
        );

        return permissionRepository.save(permission);
    }

    /**
     * Get permission.
     */
    @Transactional(readOnly = true)
    public Permission getPermission(UUID id) {

        return permissionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Permission not found"
                        ));
    }

    /**
     * Get all permissions.
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {

        return permissionRepository
                .findAllByOrderByModuleKeyAscNameAsc();
    }

    /**
     * Get permissions for a module.
     */
    @Transactional(readOnly = true)
    public List<Permission> getModulePermissions(
            String moduleKey) {

        return permissionRepository
                .findAllByModuleKeyOrderByNameAsc(
                        moduleKey
                );
    }

    /**
     * Update permission metadata.
     *
     * Permission key, module and action are immutable
     * after creation because they are security identifiers.
     */
    public Permission updatePermission(
            UUID id,
            UpdatePermissionRequest request) {

        Permission permission =
                getPermission(id);

        permission.setName(request.name());
        permission.setDescription(
                request.description()
        );

        return permissionRepository.save(
                permission
        );
    }
}