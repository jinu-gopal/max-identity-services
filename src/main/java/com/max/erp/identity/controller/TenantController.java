package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateTenantRequest;
import com.max.erp.identity.dto.TenantResponse;
import com.max.erp.identity.dto.UpdateTenantRequest;
import com.max.erp.identity.service.TenantService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(
                tenantService.getAllTenantResponses()
        );
    }

    /**
     * Get tenant by ID.
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenant(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                tenantService.getTenantResponse(tenantId)
        );
    }

    /**
     * Create tenant. Supports both JSON request body and URL query parameters.
     */
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody(required = false) CreateTenantRequest bodyRequest,
            @RequestParam(required = false) String tenantKey,
            @RequestParam(required = false) String name) {

        CreateTenantRequest request;
        if (bodyRequest != null) {
            request = bodyRequest;
        } else if (tenantKey != null && name != null) {
            request = new CreateTenantRequest(tenantKey, name);
        } else {
            throw new IllegalArgumentException("Tenant key and name are required");
        }

        TenantResponse response = tenantService.createTenantResponse(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Update tenant. Supports both JSON request body and URL query parameters.
     */
    @PutMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable UUID tenantId,
            @Valid @RequestBody(required = false) UpdateTenantRequest bodyRequest,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {

        UpdateTenantRequest request;
        if (bodyRequest != null) {
            request = bodyRequest;
        } else {
            request = new UpdateTenantRequest(name, status);
        }

        return ResponseEntity.ok(
                tenantService.updateTenantResponse(tenantId, request)
        );
    }
}