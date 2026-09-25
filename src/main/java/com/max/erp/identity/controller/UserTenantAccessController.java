package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.UserTenantAccessRequest;
import com.max.erp.identity.dto.UserTenantAccessResponse;
import com.max.erp.identity.service.UserTenantAccessService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserTenantAccessController {

    private final UserTenantAccessService userTenantAccessService;
    private final SecurityAuthorizationGuard authorizationGuard;

    public UserTenantAccessController(
            UserTenantAccessService userTenantAccessService,
            SecurityAuthorizationGuard authorizationGuard) {

        this.userTenantAccessService = userTenantAccessService;
        this.authorizationGuard = authorizationGuard;
    }

    @GetMapping("/{userId}/tenant-access")
    public List<UserTenantAccessResponse> getUserTenantAccess(
            @PathVariable UUID userId,
            @RequestParam UUID tenantId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                null,
                "SECURITY",
                "ACCESS_MANAGE"
        );

        return userTenantAccessService
                .getUserAccess(userId)
                .stream()
                .map(UserTenantAccessResponse::from)
                .toList();
    }

    @PostMapping("/{userId}/tenant-access")
    public UserTenantAccessResponse grantTenantAccess(
            @PathVariable UUID userId,
            @Valid @RequestBody UserTenantAccessRequest request,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                request.tenantId(),
                request.organizationId(),
                "SECURITY",
                "ACCESS_MANAGE"
        );

        return UserTenantAccessResponse.from(
                userTenantAccessService.grantAccess(
                        userId,
                        request.tenantId(),
                        request.organizationId()
                )
        );
    }

    @DeleteMapping("/{userId}/tenant-access")
    public void revokeTenantAccess(
            @PathVariable UUID userId,
            @RequestParam UUID tenantId,
            @RequestParam(required = false) UUID organizationId,
            Authentication authentication) {

        authorizationGuard.require(
                authentication,
                tenantId,
                organizationId,
                "SECURITY",
                "ACCESS_MANAGE"
        );

        userTenantAccessService.revokeAccess(
                userId,
                tenantId,
                organizationId
        );
    }
}