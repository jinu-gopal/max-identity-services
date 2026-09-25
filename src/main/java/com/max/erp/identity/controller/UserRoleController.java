package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.UserRoleAssignmentRequest;
import com.max.erp.identity.dto.UserRoleResponse;
import com.max.erp.identity.service.UserRoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final SecurityAuthorizationGuard authorizationGuard;

    public UserRoleController(
            UserRoleService userRoleService,
            SecurityAuthorizationGuard authorizationGuard) {

        this.userRoleService = userRoleService;
        this.authorizationGuard = authorizationGuard;
    }

    @GetMapping
    public ResponseEntity<List<UserRoleResponse>> getUserRoles(
            @PathVariable UUID userId,
            @RequestParam UUID tenantId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "USER_ROLE_ASSIGN"
        );

        List<UserRoleResponse> response =
                userRoleService
                        .getUserRoles(userId)
                        .stream()
                        .map(UserRoleResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<UserRoleResponse>> getUserTenantRoles(
            @PathVariable UUID userId,
            @PathVariable UUID tenantId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "USER_ROLE_ASSIGN"
        );

        List<UserRoleResponse> response =
                userRoleService
                        .getUserTenantRoles(
                                userId,
                                tenantId
                        )
                        .stream()
                        .map(UserRoleResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserRoleResponse> assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody
            UserRoleAssignmentRequest request,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                request.tenantId(),
                request.organizationId(),
                "SECURITY",
                "USER_ROLE_ASSIGN"
        );

        var userRole =
                userRoleService.assignRole(
                        userId,
                        request.roleId(),
                        request.tenantId(),
                        request.organizationId()
                );

        return ResponseEntity.ok(
                UserRoleResponse.from(userRole)
        );
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> revokeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId,
            @RequestParam UUID tenantId,
            @RequestParam(required = false)
            UUID organizationId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                organizationId,
                "SECURITY",
                "USER_ROLE_ASSIGN"
        );

        userRoleService.revokeRole(
                userId,
                roleId,
                tenantId,
                organizationId
        );

        return ResponseEntity.noContent().build();
    }
}