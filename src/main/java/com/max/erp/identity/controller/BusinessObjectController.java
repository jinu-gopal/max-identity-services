package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectResponse;
import com.max.erp.identity.dto.CreateBusinessObjectRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectRequest;
import com.max.erp.identity.service.BusinessObjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/configuration/business-objects")
public class BusinessObjectController {

    private final BusinessObjectService businessObjectService;

    public BusinessObjectController(
            BusinessObjectService businessObjectService) {

        this.businessObjectService = businessObjectService;
    }

    /**
     * Get all business objects.
     *
     * GET /api/v1/configuration/business-objects
     */
    @GetMapping
    public ResponseEntity<List<BusinessObjectResponse>> getAll() {

        return ResponseEntity.ok(
                businessObjectService.getAll()
        );
    }

    /**
     * Create a new business object.
     *
     * POST /api/v1/configuration/business-objects
     */
    @PostMapping
    public ResponseEntity<BusinessObjectResponse> create(
            @Valid @RequestBody CreateBusinessObjectRequest request) {

        BusinessObjectResponse response =
                businessObjectService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Update a DRAFT business object.
     *
     * PUT /api/v1/configuration/business-objects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusinessObjectResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusinessObjectRequest request) {

        BusinessObjectResponse response =
                businessObjectService.update(id, request);

        return ResponseEntity.ok(response);
    }
}