package com.max.erp.identity.controller;

import com.max.erp.identity.entity.ModuleEntitlement;
import com.max.erp.identity.service.ModuleEntitlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/tenants/{tenantId}/modules"
)
public class ModuleEntitlementController {

    private final ModuleEntitlementService entitlementService;

    public ModuleEntitlementController(
            ModuleEntitlementService entitlementService) {

        this.entitlementService = entitlementService;
    }

    /**
     * Get all module entitlements for tenant.
     */
    @GetMapping
    public ResponseEntity<List<ModuleEntitlement>>
    getTenantEntitlements(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                entitlementService
                        .getTenantEntitlements(tenantId)
        );
    }

    /**
     * Get enabled modules for tenant.
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<ModuleEntitlement>>
    getEnabledModules(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                entitlementService
                        .getEnabledModules(tenantId)
        );
    }

    /**
     * Enable module for tenant.
     */
    @PutMapping("/{moduleId}/enable")
    public ResponseEntity<ModuleEntitlement>
    enableModule(
            @PathVariable UUID tenantId,
            @PathVariable UUID moduleId) {

        return ResponseEntity.ok(
                entitlementService.enableModule(
                        tenantId,
                        moduleId
                )
        );
    }

    /**
     * Disable module for tenant.
     */
    @PutMapping("/{moduleId}/disable")
    public ResponseEntity<ModuleEntitlement>
    disableModule(
            @PathVariable UUID tenantId,
            @PathVariable UUID moduleId) {

        return ResponseEntity.ok(
                entitlementService.disableModule(
                        tenantId,
                        moduleId
                )
        );
    }

    /**
     * Check whether a module is enabled.
     */
    @GetMapping("/{moduleId}/enabled")
    public ResponseEntity<Boolean>
    isModuleEnabled(
            @PathVariable UUID tenantId,
            @PathVariable UUID moduleId) {

        return ResponseEntity.ok(
                entitlementService.isModuleEnabled(
                        tenantId,
                        moduleId
                )
        );
    }
}