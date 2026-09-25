package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateOrganizationRequest;
import com.max.erp.identity.dto.UpdateOrganizationRequest;
import com.max.erp.identity.entity.Organization;
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
    public ResponseEntity<List<Organization>>
    getOrganizations(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                organizationService
                        .getOrganizations(tenantId)
        );
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization>
    getOrganization(
            @PathVariable UUID organizationId) {

        return ResponseEntity.ok(
                organizationService
                        .getOrganization(organizationId)
        );
    }

    @PostMapping
    public ResponseEntity<Organization>
    createOrganization(
            @PathVariable UUID tenantId,
            @Valid @RequestBody
                    CreateOrganizationRequest request) {

        Organization organization =
                organizationService.createOrganization(
                        tenantId,
                        request.organizationKey(),
                        request.name(),
                        request.organizationType(),
                        request.parentOrganizationId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(organization);
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<Organization>
    updateOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody
                    UpdateOrganizationRequest request) {

        return ResponseEntity.ok(
                organizationService.updateOrganization(
                        organizationId,
                        request.name(),
                        request.status()
                )
        );
    }
}