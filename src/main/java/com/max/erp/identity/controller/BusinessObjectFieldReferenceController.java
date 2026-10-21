package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectFieldReferenceResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.service.BusinessObjectFieldReferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/reference"
)
public class BusinessObjectFieldReferenceController {

    private final BusinessObjectFieldReferenceService referenceService;

    public BusinessObjectFieldReferenceController(
            BusinessObjectFieldReferenceService referenceService
    ) {
        this.referenceService = referenceService;
    }

    @GetMapping
    public ResponseEntity<BusinessObjectFieldReferenceResponse> getReference(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId
    ) {
        return ResponseEntity.ok(
                referenceService.getReference(
                        businessObjectId,
                        fieldId
                )
        );
    }

    @PostMapping
    public ResponseEntity<BusinessObjectFieldReferenceResponse> createReference(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId,
            @Valid @RequestBody CreateBusinessObjectFieldReferenceRequest request
    ) {
        return ResponseEntity.ok(
                referenceService.createReference(
                        businessObjectId,
                        fieldId,
                        request
                )
        );
    }

    @PutMapping("/{referenceId}")
    public ResponseEntity<BusinessObjectFieldReferenceResponse> updateReference(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId,
            @PathVariable UUID referenceId,
            @Valid @RequestBody UpdateBusinessObjectFieldReferenceRequest request
    ) {
        return ResponseEntity.ok(
                referenceService.updateReference(
                        businessObjectId,
                        fieldId,
                        referenceId,
                        request
                )
        );
    }

    @DeleteMapping("/{referenceId}")
    public ResponseEntity<Void> deleteReference(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId,
            @PathVariable UUID referenceId
    ) {
        referenceService.deleteReference(
                businessObjectId,
                fieldId,
                referenceId
        );

        return ResponseEntity.noContent().build();
    }
}