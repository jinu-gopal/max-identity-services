package com.max.erp.identity.service;

import com.max.erp.identity.entity.Organization;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.entity.UserRole;
import com.max.erp.identity.repository.OrganizationRepository;
import com.max.erp.identity.repository.RoleRepository;
import com.max.erp.identity.repository.TenantRepository;
import com.max.erp.identity.repository.UserRepository;
import com.max.erp.identity.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserTenantAccessService userTenantAccessService;

    public UserRoleService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            OrganizationRepository organizationRepository,
            UserRoleRepository userRoleRepository,
            UserTenantAccessService userTenantAccessService) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.organizationRepository = organizationRepository;
        this.userRoleRepository = userRoleRepository;
        this.userTenantAccessService = userTenantAccessService;
    }

    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(UUID userId) {

        getUser(userId);

        return userRoleRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<UserRole> getUserTenantRoles(
            UUID userId,
            UUID tenantId) {

        getUser(userId);
        getTenant(tenantId);

        return userRoleRepository
                .findAllByUserIdAndTenantId(
                        userId,
                        tenantId
                );
    }

    public UserRole assignRole(
            UUID userId,
            UUID roleId,
            UUID tenantId,
            UUID organizationId) {

        User user = getUser(userId);
        Tenant tenant = getTenant(tenantId);
        Role role = getRole(roleId);

        validateUserActive(user);
        validateTenantActive(tenant);
        validateRoleActive(role);

        /*
         * A tenant-scoped role belongs to exactly one tenant.
         * It cannot be assigned to another tenant.
         */
        if (role.getTenant() != null &&
                !role.getTenant().getId().equals(tenantId)) {

            throw new IllegalArgumentException(
                    "Role does not belong to the specified tenant"
            );
        }

        /*
         * Global roles have no tenant owner.
         * They may be assigned within a tenant.
         */
        validateOrganization(
                tenantId,
                organizationId
        );

        /*
         * A role assignment is only meaningful when the
         * user has access to the tenant. If not granted yet,
         * grant active tenant access automatically.
         */
        if (!userTenantAccessService.hasTenantAccess(
                userId,
                tenantId
        )) {
            userTenantAccessService.grantAccess(
                    userId,
                    tenantId,
                    null
            );
        }

        if (userRoleRepository
                .existsByUserIdAndRoleIdAndTenantIdAndOrganizationId(
                        userId,
                        roleId,
                        tenantId,
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "Role is already assigned to this user"
            );
        }

        UserRole userRole = new UserRole();

        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setTenant(tenant);
        userRole.setOrganizationId(organizationId);
        userRole.setStatus("ACTIVE");

        return userRoleRepository.save(userRole);
    }

    public void revokeRole(
            UUID userId,
            UUID roleId,
            UUID tenantId,
            UUID organizationId) {

        Role role = getRole(roleId);

        if (Boolean.TRUE.equals(role.isSystem())) {

            throw new IllegalArgumentException(
                    "System role assignments cannot be modified"
            );
        }

        UserRole userRole =
                userRoleRepository
                        .findByUserIdAndRoleIdAndTenantIdAndOrganizationId(
                                userId,
                                roleId,
                                tenantId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Role assignment not found"
                                ));

        userRole.setStatus("INACTIVE");

        userRoleRepository.save(userRole);
    }

    private User getUser(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));
    }

    private Tenant getTenant(UUID tenantId) {

        return tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found"
                        ));
    }

    private Role getRole(UUID roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found"
                        ));
    }

    private void validateUserActive(User user) {

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {

            throw new IllegalArgumentException(
                    "User is not active"
            );
        }
    }

    private void validateTenantActive(Tenant tenant) {

        if (!"ACTIVE".equalsIgnoreCase(tenant.getStatus())) {

            throw new IllegalArgumentException(
                    "Tenant is not active"
            );
        }
    }

    private void validateRoleActive(Role role) {

        if (!"ACTIVE".equalsIgnoreCase(role.getStatus())) {

            throw new IllegalArgumentException(
                    "Role is not active"
            );
        }
    }

    private void validateOrganization(
            UUID tenantId,
            UUID organizationId) {

        if (organizationId == null) {
            return;
        }

        Organization organization =
                organizationRepository
                        .findById(organizationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found"
                                ));

        if (!organization.getTenant().getId()
                .equals(tenantId)) {

            throw new IllegalArgumentException(
                    "Organization does not belong to tenant"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                organization.getStatus())) {

            throw new IllegalArgumentException(
                    "Organization is not active"
            );
        }
    }
}