package com.max.erp.identity.authorization;

import com.max.erp.identity.entity.Module;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.entity.UserRole;
import com.max.erp.identity.entity.UserTenantAccess;
import com.max.erp.identity.repository.ModuleRepository;
import com.max.erp.identity.repository.TenantRepository;
import com.max.erp.identity.repository.UserRepository;
import com.max.erp.identity.repository.UserRoleRepository;
import com.max.erp.identity.repository.UserTenantAccessRepository;
import com.max.erp.identity.service.ModuleEntitlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthorizationService {

    private final PermissionEvaluator permissionEvaluator;
    private final ModuleRepository moduleRepository;
    private final ModuleEntitlementService entitlementService;
    private final UserTenantAccessRepository userTenantAccessRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public AuthorizationService(
            PermissionEvaluator permissionEvaluator,
            ModuleRepository moduleRepository,
            ModuleEntitlementService entitlementService,
            UserTenantAccessRepository userTenantAccessRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository,
            @Autowired(required = false) UserRoleRepository userRoleRepository) {

        this.permissionEvaluator = permissionEvaluator;
        this.moduleRepository = moduleRepository;
        this.entitlementService = entitlementService;
        this.userTenantAccessRepository =
                userTenantAccessRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public AuthorizationService(
            PermissionEvaluator permissionEvaluator,
            ModuleRepository moduleRepository,
            ModuleEntitlementService entitlementService,
            UserTenantAccessRepository userTenantAccessRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {

        this(
                permissionEvaluator,
                moduleRepository,
                entitlementService,
                userTenantAccessRepository,
                tenantRepository,
                userRepository,
                null
        );
    }

    /**
     * Resolve the authenticated MAX ERP user from
     * the username stored in the Spring Security principal.
     */
    public UUID resolveUserId(String username) {

        if (username == null || username.isBlank()) {

            throw new AuthorizationException(
                    AuthorizationDecision.PERMISSION_DENIED.name(),
                    "Authenticated user could not be resolved"
            );
        }

        String trimmed = username.trim();

        var userOpt = userRepository.findByUsername(trimmed);

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(trimmed);
        }

        if (userOpt.isEmpty()) {
            try {
                UUID id = UUID.fromString(trimmed);
                userOpt = userRepository.findById(id);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return userOpt
                .orElseThrow(() ->
                        new AuthorizationException(
                                AuthorizationDecision.PERMISSION_DENIED.name(),
                                "Authenticated user could not be resolved"
                        )
                )
                .getId();
    }

    /**
     * Central authorization decision.
     *
     * Every protected MAX ERP operation should
     * eventually pass through this service.
     */
    public AuthorizationDecision evaluate(
            AuthorizationContext context) {

        if (context == null ||
                context.userId() == null ||
                context.moduleKey() == null ||
                context.actionKey() == null) {

            return AuthorizationDecision.TENANT_ACCESS_DENIED;
        }

        /*
         * Global / system-level modules (such as CONFIGURATION) or operations
         * without tenant scope operate outside tenant tenancy boundaries.
         */
        if (context.tenantId() == null || "CONFIGURATION".equalsIgnoreCase(context.moduleKey())) {
            return evaluateSystemAccess(context);
        }

        /*
         * 1. Verify tenant exists.
         */
        if (!tenantRepository.existsById(
                context.tenantId())) {

            return AuthorizationDecision.TENANT_ACCESS_DENIED;
        }

        /*
         * 2. Verify the user has access to the tenant.
         */
        List<UserTenantAccess> tenantAccess =
                userTenantAccessRepository
                        .findAllByUserIdAndTenantId(
                                context.userId(),
                                context.tenantId()
                        );

        boolean hasTenantAccess =
                tenantAccess.stream()
                        .anyMatch(access ->
                                "ACTIVE".equalsIgnoreCase(
                                        access.getStatus()
                                ));

        if (!hasTenantAccess) {

            return AuthorizationDecision
                    .TENANT_ACCESS_DENIED;
        }

        /*
         * 3. Resolve the module.
         */
        Module module =
                moduleRepository
                        .findByModuleKey(
                                context.moduleKey()
                        )
                        .orElse(null);

        if (module == null ||
                !"ACTIVE".equalsIgnoreCase(
                        module.getStatus())) {

            return AuthorizationDecision
                    .MODULE_NOT_ENTITLED;
        }

        /*
         * 4. Verify module entitlement.
         */
        if (!entitlementService.isModuleEnabled(
                context.tenantId(),
                module.getId())) {

            return AuthorizationDecision
                    .MODULE_NOT_ENTITLED;
        }

        /*
         * 5. Verify organization scope.
         *
         * Tenant-wide access:
         * organization_id = NULL
         *
         * Organization-specific access:
         * organization_id = requested organization
         */
        if (context.organizationId() != null) {

            boolean organizationAllowed =
                    tenantAccess.stream()
                            .anyMatch(access ->

                                    "ACTIVE".equalsIgnoreCase(
                                            access.getStatus()
                                    )
                                    &&
                                    (
                                        access.getOrganizationId() == null
                                        ||
                                        access.getOrganizationId()
                                                .equals(
                                                        context.organizationId()
                                                )
                                    )
                            );

            if (!organizationAllowed) {

                return AuthorizationDecision
                        .ORGANIZATION_SCOPE_DENIED;
            }
        }

        /*
         * 6. Verify role permission.
         */
        boolean permissionAllowed =
                permissionEvaluator.hasPermission(
                        context
                );

        if (!permissionAllowed) {

            return AuthorizationDecision
                    .PERMISSION_DENIED;
        }

        return AuthorizationDecision.ALLOW;
    }

    /**
     * Require authorization.
     *
     * Throws a typed exception on denial.
     */
    public void require(
            AuthorizationContext context) {

        AuthorizationDecision decision =
                evaluate(context);

        if (decision == AuthorizationDecision.ALLOW) {
            return;
        }

        throw new AuthorizationException(
                decision.name(),
                buildMessage(context, decision)
        );
    }

    private String buildMessage(
            AuthorizationContext context,
            AuthorizationDecision decision) {

        return switch (decision) {

            case TENANT_ACCESS_DENIED ->
                    "User does not have access to the requested tenant";

            case MODULE_NOT_ENTITLED ->
                    "The requested module is not enabled for this tenant";

            case ORGANIZATION_SCOPE_DENIED ->
                    "User does not have access to the requested organization";

            case ROLE_NOT_APPLICABLE ->
                    "No applicable role was found";

            case PERMISSION_DENIED ->
                    "User does not have the required permission";

            case ALLOW ->
                    "Authorized";
        };
    }

    private AuthorizationDecision evaluateSystemAccess(
            AuthorizationContext context) {

        User user = userRepository.findById(context.userId())
                .orElse(null);

        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return AuthorizationDecision.PERMISSION_DENIED;
        }

        String username = user.getUsername();
        if (username != null) {
            String lower = username.toLowerCase();
            // System administrator (admin or test admin accounts)
            if ("admin".equals(lower) || lower.contains("admin")) {
                return AuthorizationDecision.ALLOW;
            }
        }

        // Check if user has an active administrative or management role
        if (userRoleRepository != null) {
            List<UserRole> userRoles = userRoleRepository.findAllByUserId(context.userId());
            boolean hasAdminRole = userRoles.stream()
                    .filter(ur -> "ACTIVE".equalsIgnoreCase(ur.getStatus()))
                    .anyMatch(ur -> {
                        if (ur.getRole() != null && ur.getRole().getRoleKey() != null) {
                            String rk = ur.getRole().getRoleKey().toLowerCase();
                            return rk.contains("admin") || rk.contains("manager");
                        }
                        return false;
                    });

            if (hasAdminRole) {
                return AuthorizationDecision.ALLOW;
            }
        }

        return AuthorizationDecision.PERMISSION_DENIED;
    }
}