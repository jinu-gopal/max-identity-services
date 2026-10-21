package com.max.erp.identity.service;

import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessObjectRecordServiceTest {

    private BusinessObjectRecordRepository recordRepository;
    private BusinessObjectRepository businessObjectRepository;
    private BusinessObjectFieldRepository fieldRepository;
    private RecordValidationEngine validationEngine;
    private BusinessObjectRecordService recordService;

    private UUID businessObjectId;
    private UUID recordId;
    private BusinessObject businessObject;

    @BeforeEach
    void setUp() {
        recordRepository = mock(BusinessObjectRecordRepository.class);
        businessObjectRepository = mock(BusinessObjectRepository.class);
        fieldRepository = mock(BusinessObjectFieldRepository.class);
        validationEngine = mock(RecordValidationEngine.class);

        recordService = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        businessObjectId = UUID.randomUUID();
        recordId = UUID.randomUUID();

        businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);
        businessObject.setName("Customer");
        businessObject.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("getRecords returns list of records for business object")
    void getRecordsReturnsList() {
        BusinessObjectRecord r1 = createRecordEntity(UUID.randomUUID(), businessObjectId, Map.of("name", "Acme"));
        BusinessObjectRecord r2 = createRecordEntity(UUID.randomUUID(), businessObjectId, Map.of("name", "Beta"));

        when(recordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId))
                .thenReturn(List.of(r1, r2));

        List<BusinessObjectRecord> records = recordService.getRecords(businessObjectId);

        assertEquals(2, records.size());
        assertEquals("Acme", records.get(0).getRecordData().get("name"));
    }

    @Test
    @DisplayName("getRecord returns record when ID and businessObjectId match")
    void getRecordReturnsRecord() {
        BusinessObjectRecord record = createRecordEntity(recordId, businessObjectId, Map.of("name", "Acme"));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        BusinessObjectRecord result = recordService.getRecord(businessObjectId, recordId);

        assertNotNull(result);
        assertEquals(recordId, result.getId());
    }

    @Test
    @DisplayName("getRecord throws 404 NOT_FOUND when record is not found")
    void getRecordThrows404WhenNotFound() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                recordService.getRecord(businessObjectId, recordId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("getRecord throws 404 NOT_FOUND when record belongs to different business object")
    void getRecordThrows404WhenBusinessObjectMismatch() {
        UUID otherObjectId = UUID.randomUUID();
        BusinessObjectRecord record = createRecordEntity(recordId, otherObjectId, Map.of("name", "Acme"));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                recordService.getRecord(businessObjectId, recordId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("createRecord successfully validates and saves record")
    void createRecordSuccess() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(Collections.emptyList());
        when(validationEngine.validate(any())).thenReturn(ValidationResult.success());

        Map<String, Object> data = Map.of("company_name", "Acme Corp", "customer_type", "Corporate");

        when(recordRepository.save(any(BusinessObjectRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BusinessObjectRecord created = recordService.createRecord(businessObjectId, data, null);

        assertNotNull(created);
        assertEquals(businessObjectId, created.getBusinessObjectId());
        assertEquals("Acme Corp", created.getRecordData().get("company_name"));
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        verify(recordRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createRecord throws 400 BAD_REQUEST when recordData is null")
    void createRecordThrows400WhenDataNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                recordService.createRecord(businessObjectId, null, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("createRecord throws RecordValidationException when validation fails")
    void createRecordThrowsValidationException() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(Collections.emptyList());

        ValidationError error = new ValidationError("company_name", "REQUIRED", "Company Name is required");
        when(validationEngine.validate(any())).thenReturn(ValidationResult.failure(List.of(error)));

        Map<String, Object> data = Map.of("customer_type", "Corporate");

        RecordValidationException ex = assertThrows(RecordValidationException.class, () ->
                recordService.createRecord(businessObjectId, data, null)
        );

        assertEquals(1, ex.getErrors().size());
        assertEquals("company_name", ex.getErrors().get(0).fieldKey());
        verify(recordRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRecord successfully merges data and updates record")
    void updateRecordSuccess() {
        Map<String, Object> existingData = new LinkedHashMap<>();
        existingData.put("company_name", "Acme Old");
        existingData.put("status", "Active");

        BusinessObjectRecord existing = createRecordEntity(recordId, businessObjectId, existingData);
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(existing));
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(Collections.emptyList());
        when(validationEngine.validate(any())).thenReturn(ValidationResult.success());
        when(recordRepository.save(any(BusinessObjectRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updateData = Map.of("company_name", "Acme New");

        BusinessObjectRecord updated = recordService.updateRecord(businessObjectId, recordId, updateData);

        assertNotNull(updated);
        assertEquals("Acme New", updated.getRecordData().get("company_name"));
        assertEquals("Active", updated.getRecordData().get("status"));
    }

    @Test
    @DisplayName("deleteRecord removes record from repository")
    void deleteRecordSuccess() {
        BusinessObjectRecord record = createRecordEntity(recordId, businessObjectId, Map.of("name", "Acme"));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        recordService.deleteRecord(businessObjectId, recordId);

        verify(recordRepository, times(1)).delete(record);
    }

    private BusinessObjectRecord createRecordEntity(UUID id, UUID objId, Map<String, Object> data) {
        BusinessObjectRecord r = new BusinessObjectRecord();
        r.setId(id);
        r.setBusinessObjectId(objId);
        r.setRecordData(data);
        r.setCreatedAt(OffsetDateTime.now());
        r.setUpdatedAt(OffsetDateTime.now());
        return r;
    }
}
