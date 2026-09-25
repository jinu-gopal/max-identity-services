package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectFieldOptionResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.service.BusinessObjectFieldOptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/options"
)
public class BusinessObjectFieldOptionController {

    private final BusinessObjectFieldOptionService optionService;

    public BusinessObjectFieldOptionController(
            BusinessObjectFieldOptionService optionService) {

        this.optionService = optionService;
    }

    // ============================================================
    // GET ALL OPTIONS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<BusinessObjectFieldOptionResponse>> getOptions(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId) {

        return ResponseEntity.ok(
                optionService.getOptions(
                        businessObjectId,
                        fieldId
                )
        );
    }

    // ============================================================
    // CREATE OPTION
    // ============================================================

    @PostMapping
    public ResponseEntity<BusinessObjectFieldOptionResponse> createOption(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId,
            @Valid @RequestBody CreateBusinessObjectFieldOptionRequest request) {

        BusinessObjectFieldOptionResponse response =
                optionService.createOption(
                        businessObjectId,
                        fieldId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE OPTION
    // ============================================================

    @PutMapping("/{optionId}")
    public ResponseEntity<BusinessObjectFieldOptionResponse> updateOption(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId,
            @PathVariable UUID optionId,
            @Valid @RequestBody UpdateBusinessObjectFieldOptionRequest request) {

        BusinessObjectFieldOptionResponse response =
                optionService.updateOption(
                        businessObjectId,
                        fieldId,
                        optionId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // DELETE OPTION
    // ============================================================

    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deleteOption(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID fieldId,
            @PathVariable UUID optionId) {

        optionService.deleteOption(
                businessObjectId,
                fieldId,
                optionId
        );

        return ResponseEntity.noContent().build();
    }
}