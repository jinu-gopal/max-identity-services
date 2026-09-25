package com.max.erp.identity.controller;

import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Get all tenants.
     */
    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {

        return ResponseEntity.ok(
                tenantService.getAllTenants()
        );
    }

    /**
     * Get tenant by ID.
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<Tenant> getTenant(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                tenantService.getTenant(tenantId)
        );
    }

    /**
     * Create tenant.
     */
    @PostMapping
    public ResponseEntity<Tenant> createTenant(
            @RequestParam String tenantKey,
            @RequestParam String name) {

        Tenant tenant =
                tenantService.createTenant(
                        tenantKey,
                        name
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenant);
    }

    /**
     * Update tenant.
     */
    @PutMapping("/{tenantId}")
    public ResponseEntity<Tenant> updateTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                tenantService.updateTenant(
                        tenantId,
                        name,
                        status
                )
        );
    }
}