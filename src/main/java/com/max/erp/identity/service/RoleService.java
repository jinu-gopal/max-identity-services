package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateRoleRequest;
import com.max.erp.identity.dto.UpdateRoleRequest;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.RoleRepository;
import com.max.erp.identity.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    public RoleService(
            RoleRepository roleRepository,
            TenantRepository tenantRepository) {

        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Create a global or tenant-specific role.
     */
    public Role createRole(CreateRoleRequest request) {

        Tenant tenant = null;

        if (request.tenantId() != null) {

            tenant = tenantRepository.findById(
                    request.tenantId()
            ).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Tenant not found"
                    ));

            if (roleRepository
                    .existsByTenantIdAndRoleKey(
                            request.tenantId(),
                            request.roleKey())) {

                throw new IllegalArgumentException(
                        "Role key already exists for this tenant"
                );
            }

        } else {

            if (roleRepository
                    .existsByRoleKeyAndTenantIsNull(
                            request.roleKey())) {

                throw new IllegalArgumentException(
                        "Global role key already exists"
                );
            }
        }

        Role role = new Role();

        role.setRoleKey(request.roleKey());
        role.setName(request.name());
        role.setDescription(request.description());
        role.setTenant(tenant);

        /*
         * Roles created through the administration API
         * are never system roles.
         */
        role.setSystem(false);
        role.setStatus("ACTIVE");

        return roleRepository.save(role);
    }

    /**
     * Get a role.
     */
    @Transactional(readOnly = true)
    public Role getRole(UUID id) {

        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found"
                        ));
    }

    /**
     * Get all global roles.
     */
    @Transactional(readOnly = true)
    public List<Role> getGlobalRoles() {

        return roleRepository
                .findAllByTenantIsNullOrderByNameAsc();
    }

    /**
     * Get all roles available to a tenant.
     *
     * Includes:
     * 1. Tenant-specific roles belonging to the tenant.
     * 2. Global roles that can be used by the tenant.
     *
     * Global roles are not automatically assigned.
     */
    @Transactional(readOnly = true)
    public List<Role> getTenantRoles(UUID tenantId) {

        if (!tenantRepository.existsById(tenantId)) {

            throw new IllegalArgumentException(
                    "Tenant not found"
            );
        }

        return roleRepository
                .findAllByTenantIdOrTenantIsNullOrderByNameAsc(
                        tenantId
                );
    }

    /**
     * Update role metadata.
     *
     * System roles cannot be modified.
     */
    public Role updateRole(
            UUID id,
            UpdateRoleRequest request) {

        Role role = getRole(id);

        if (Boolean.TRUE.equals(role.isSystem())) {

            throw new IllegalArgumentException(
                    "System roles cannot be modified"
            );
        }

        /*
         * Global roles are platform-level definitions.
         * Tenant administrators must not modify them.
         *
         * This protection will later be enforced using the
         * authorization context and platform-level permissions.
         */

        role.setName(request.name());
        role.setDescription(request.description());
        role.setStatus(request.status());

        return roleRepository.save(role);
    }
}