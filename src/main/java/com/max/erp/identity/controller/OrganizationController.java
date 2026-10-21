package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateOrganizationRequest;
import com.max.erp.identity.dto.OrganizationResponse;
import com.max.erp.identity.dto.UpdateOrganizationRequest;
import com.max.erp.identity.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(
            OrganizationService organizationService) {

        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>>
    getOrganizations(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                organizationService
                        .getOrganizationResponses(tenantId)
        );
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<OrganizationResponse>
    getOrganization(
            @PathVariable UUID tenantId,
            @PathVariable UUID organizationId) {

        return ResponseEntity.ok(
                organizationService
                        .getOrganizationResponse(tenantId, organizationId)
        );
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse>
    createOrganization(
            @PathVariable UUID tenantId,
            @Valid @RequestBody
                    CreateOrganizationRequest request) {

        OrganizationResponse response =
                organizationService.createOrganizationResponse(
                        tenantId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<OrganizationResponse>
    updateOrganization(
            @PathVariable UUID tenantId,
            @PathVariable UUID organizationId,
            @Valid @RequestBody
                    UpdateOrganizationRequest request) {

        return ResponseEntity.ok(
                organizationService.updateOrganization(
                        tenantId,
                        organizationId,
                        request
                )
        );
    }
}