package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.CreateRoleRequest;
import com.max.erp.identity.dto.RoleResponse;
import com.max.erp.identity.dto.UpdateRoleRequest;
import com.max.erp.identity.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final SecurityAuthorizationGuard authorizationGuard;

    public RoleController(
            RoleService roleService,
            SecurityAuthorizationGuard authorizationGuard) {

        this.roleService = roleService;
        this.authorizationGuard = authorizationGuard;
    }

    @GetMapping
    public List<RoleResponse> getRoles(
            @RequestParam UUID tenantId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "ROLE_MANAGE"
        );

        return roleService
                .getTenantRoles(tenantId)
                .stream()
                .map(role -> RoleResponse.from(role, tenantId))
                .toList();
    }

    @GetMapping("/{roleId}")
    public RoleResponse getRole(
            @PathVariable UUID roleId,
            @RequestParam UUID tenantId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "ROLE_MANAGE"
        );

        return RoleResponse.from(
                roleService.getRole(roleId),
                tenantId
        );
    }

    @PostMapping
    public RoleResponse createRole(
            @Valid @RequestBody CreateRoleRequest request,
            Authentication authentication) {

        UUID tenantId = request.tenantId();

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "ROLE_MANAGE"
        );

        return RoleResponse.from(
                roleService.createRole(request),
                tenantId
        );
    }

    @PutMapping("/{roleId}")
    public RoleResponse updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request,
            @RequestParam UUID tenantId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "ROLE_MANAGE"
        );

        return RoleResponse.from(
                roleService.updateRole(
                        roleId,
                        request
                ),
                tenantId
        );
    }
}