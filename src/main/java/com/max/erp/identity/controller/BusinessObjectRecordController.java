package com.max.erp.identity.controller;

import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.service.BusinessObjectRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/configuration/business-objects/{businessObjectId}/records"
)
public class BusinessObjectRecordController {

    private final BusinessObjectRecordService recordService;

    public BusinessObjectRecordController(
            BusinessObjectRecordService recordService
    ) {
        this.recordService = recordService;
    }

    @GetMapping
    public ResponseEntity<List<BusinessObjectRecord>> getRecords(
            @PathVariable UUID businessObjectId
    ) {
        return ResponseEntity.ok(
                recordService.getRecords(businessObjectId)
        );
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<BusinessObjectRecord> getRecord(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID recordId
    ) {
        return ResponseEntity.ok(
                recordService.getRecord(
                        businessObjectId,
                        recordId
                )
        );
    }

    @PostMapping
    public ResponseEntity<BusinessObjectRecord> createRecord(
            @PathVariable UUID businessObjectId,
            @RequestBody Map<String, Object> recordData
    ) {
        BusinessObjectRecord record =
                recordService.createRecord(
                        businessObjectId,
                        recordData,
                        null
                );

        return ResponseEntity.ok(record);
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<BusinessObjectRecord> updateRecord(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID recordId,
            @RequestBody Map<String, Object> recordData
    ) {
        BusinessObjectRecord record =
                recordService.updateRecord(
                        businessObjectId,
                        recordId,
                        recordData
                );

        return ResponseEntity.ok(record);
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable UUID businessObjectId,
            @PathVariable UUID recordId
    ) {
        recordService.deleteRecord(
                businessObjectId,
                recordId
        );

        return ResponseEntity.noContent().build();
    }
}