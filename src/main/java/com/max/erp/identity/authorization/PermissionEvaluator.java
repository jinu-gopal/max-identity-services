package com.max.erp.identity.authorization;

import com.max.erp.identity.entity.Permission;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.entity.RolePermission;
import com.max.erp.identity.entity.UserRole;
import com.max.erp.identity.repository.RolePermissionRepository;
import com.max.erp.identity.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionEvaluator {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public PermissionEvaluator(
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository) {

        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public boolean hasPermission(
            AuthorizationContext context) {

        List<UserRole> userRoles =
                userRoleRepository
                        .findAllByUserIdAndTenantId(
                                context.userId(),
                                context.tenantId()
                        );

        for (UserRole userRole : userRoles) {

            if (!isRoleInScope(userRole, context)) {
                continue;
            }

            Role role = userRole.getRole();

            List<RolePermission> rolePermissions =
                    rolePermissionRepository
                            .findAllByRoleId(role.getId());

            for (RolePermission rolePermission :
                    rolePermissions) {

                Permission permission =
                        rolePermission.getPermission();

                if (permission == null) {
                    continue;
                }

                if (permission.getModuleKey()
                        .equalsIgnoreCase(
                                context.moduleKey())
                        &&
                    permission.getActionKey()
                        .equalsIgnoreCase(
                                context.actionKey())) {

                    return true;
                }
            }
        }

        return false;
    }

    private boolean isRoleInScope(
            UserRole userRole,
            AuthorizationContext context) {

        if (userRole.getStatus() == null ||
                !"ACTIVE".equalsIgnoreCase(
                        userRole.getStatus())) {

            return false;
        }

        /*
         * Tenant-specific role assignment.
         *
         * UserRole stores the tenant as a Tenant
         * relationship, so compare through getTenant().
         */
        if (userRole.getTenant() != null) {

            if (userRole.getTenant().getId() == null ||
                    !userRole.getTenant()
                            .getId()
                            .equals(context.tenantId())) {

                return false;
            }
        }

        /*
         * Organization-scoped role.
         *
         * A null organization means the role applies
         * across the tenant.
         */
        if (userRole.getOrganizationId() != null &&
                !userRole.getOrganizationId()
                        .equals(context.organizationId())) {

            return false;
        }

        return true;
    }
}