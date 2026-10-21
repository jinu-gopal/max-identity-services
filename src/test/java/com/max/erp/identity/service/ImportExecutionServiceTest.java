package com.max.erp.identity.service;

import com.max.erp.identity.dto.ImportExecutionResponse;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.entity.ConfigurationImportError;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.entity.ConfigurationImportRow;
import com.max.erp.identity.enums.ImportJobStatus;
import com.max.erp.identity.enums.ImportRowStatus;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.ConfigurationImportErrorRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.repository.ConfigurationImportRowRepository;
import com.max.erp.identity.validation.RecordValidationEngine;
import com.max.erp.identity.validation.ValidationError;
import com.max.erp.identity.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportExecutionServiceTest {

    @Mock
    private ConfigurationImportJobRepository importJobRepository;

    @Mock
    private ConfigurationImportRowRepository importRowRepository;

    @Mock
    private ConfigurationImportErrorRepository importErrorRepository;

    @Mock
    private BusinessObjectRepository businessObjectRepository;

    @Mock
    private BusinessObjectFieldRepository fieldRepository;

    @Mock
    private BusinessObjectRecordRepository recordRepository;

    @Mock
    private RecordValidationEngine validationEngine;

    @Mock
    private ImportFieldValueValidator fieldValueValidator;

    private ImportExecutionService executionService;

    private UUID businessObjectId;
    private UUID importJobId;
    private BusinessObject businessObject;
    private BusinessObjectField codeField;
    private BusinessObjectField nameField;
    private ConfigurationImportJob job;

    @BeforeEach
    void setUp() {
        executionService = new ImportExecutionService(
                importJobRepository,
                importRowRepository,
                importErrorRepository,
                businessObjectRepository,
                fieldRepository,
                recordRepository,
                validationEngine,
                fieldValueValidator
        );

        businessObjectId = UUID.randomUUID();
        importJobId = UUID.randomUUID();

        businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);
        businessObject.setObjectKey("product");
        businessObject.setName("Product");
        businessObject.setStatus("PUBLISHED");

        codeField = new BusinessObjectField();
        codeField.setId(UUID.randomUUID());
        codeField.setBusinessObject(businessObject);
        codeField.setFieldKey("productCode");
        codeField.setLabel("Product Code");
        codeField.setFieldType("TEXT");
        codeField.setRequired(true);
        codeField.setUniqueValue(true);
        codeField.setDisplayOrder(1);

        nameField = new BusinessObjectField();
        nameField.setId(UUID.randomUUID());
        nameField.setBusinessObject(businessObject);
        nameField.setFieldKey("productName");
        nameField.setLabel("Product Name");
        nameField.setFieldType("TEXT");
        nameField.setRequired(true);
        nameField.setUniqueValue(false);
        nameField.setDisplayOrder(2);

        job = new ConfigurationImportJob();
        job.setBusinessObjectId(businessObjectId);
        job.setStatus(ImportJobStatus.VALIDATED);
        job.setTotalRows(2);
        job.setValidRows(2);
        job.setInvalidRows(0);
        job.setDuplicateRows(0);
        job.setProcessedRows(0);

        Map<String, Object> columnMapping = new HashMap<>();
        columnMapping.put("Code", codeField.getId().toString());
        columnMapping.put("Name", nameField.getId().toString());
        job.setColumnMapping(columnMapping);
    }

    @Test
    void rejectWhenJobNotValidated() {
        job.setStatus(ImportJobStatus.MAPPING);
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> executionService.execute(businessObjectId, importJobId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("not in a VALIDATED state"));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void rejectWhenBusinessObjectArchived() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(1);

        businessObject.setStatus("ARCHIVED");
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> executionService.execute(businessObjectId, importJobId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("archived"));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void successfulGenericRecordCreation() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(1);
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(codeField, nameField));

        ConfigurationImportRow row1 = new ConfigurationImportRow();
        row1.setImportJobId(importJobId);
        row1.setRowNumber(1);
        row1.setStatus(ImportRowStatus.VALID);
        row1.setNormalizedData(Map.of("productCode", "P-101", "productName", "Widget A"));

        ConfigurationImportRow row2 = new ConfigurationImportRow();
        row2.setImportJobId(importJobId);
        row2.setRowNumber(2);
        row2.setStatus(ImportRowStatus.VALID);
        row2.setNormalizedData(Map.of("productCode", "P-102", "productName", "Widget B"));

        when(importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(importJobId))
                .thenReturn(List.of(row1, row2));
        when(validationEngine.validate(any())).thenReturn(ValidationResult.success());

        when(recordRepository.save(any(BusinessObjectRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UUID executorUserId = UUID.randomUUID();
        ImportExecutionResponse response = executionService.execute(businessObjectId, importJobId, executorUserId);

        assertNotNull(response);
        assertEquals(ImportJobStatus.COMPLETED, response.getStatus());
        assertEquals(2, response.getProcessedRows());
        assertEquals(0, response.getFailedRows());
        assertEquals(0, response.getDuplicateRows());
        assertEquals(2, response.getCreatedRecordIds().size());

        assertEquals(ImportRowStatus.IMPORTED, row1.getStatus());
        assertEquals(ImportRowStatus.IMPORTED, row2.getStatus());

        ArgumentCaptor<BusinessObjectRecord> recordCaptor = ArgumentCaptor.forClass(BusinessObjectRecord.class);
        verify(recordRepository, times(2)).save(recordCaptor.capture());

        List<BusinessObjectRecord> savedRecords = recordCaptor.getAllValues();
        assertEquals("P-101", savedRecords.get(0).getRecordData().get("productCode"));
        assertEquals("Widget A", savedRecords.get(0).getRecordData().get("productName"));
        assertEquals(executorUserId, savedRecords.get(0).getCreatedBy());

        assertEquals("P-102", savedRecords.get(1).getRecordData().get("productCode"));
        assertEquals("Widget B", savedRecords.get(1).getRecordData().get("productName"));
    }

    @Test
    void requiredFieldLiveValidationFailure() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(1);
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(codeField, nameField));

        ConfigurationImportRow row1 = new ConfigurationImportRow();
        row1.setImportJobId(importJobId);
        row1.setRowNumber(1);
        row1.setStatus(ImportRowStatus.VALID);
        row1.setNormalizedData(Map.of("productCode", "P-101")); // Missing required productName

        when(importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(importJobId))
                .thenReturn(List.of(row1));

        when(validationEngine.validate(any())).thenReturn(ValidationResult.failure(
                List.of(new ValidationError("productName", "REQUIRED_FIELD", "Product Name is required"))
        ));

        ImportExecutionResponse response = executionService.execute(businessObjectId, importJobId);

        assertEquals(ImportJobStatus.COMPLETED_WITH_ERRORS, response.getStatus());
        assertEquals(0, response.getProcessedRows());
        assertEquals(1, response.getFailedRows());
        assertEquals(0, response.getDuplicateRows());
        assertEquals(ImportRowStatus.FAILED, row1.getStatus());

        verify(recordRepository, never()).save(any());
        verify(importErrorRepository, times(1)).save(any(ConfigurationImportError.class));
    }

    @Test
    void intraBatchDuplicateValues() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(1);
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(codeField, nameField));

        ConfigurationImportRow row1 = new ConfigurationImportRow();
        row1.setImportJobId(importJobId);
        row1.setRowNumber(1);
        row1.setStatus(ImportRowStatus.VALID);
        row1.setNormalizedData(Map.of("productCode", "DUPE-100", "productName", "First Item"));

        ConfigurationImportRow row2 = new ConfigurationImportRow();
        row2.setImportJobId(importJobId);
        row2.setRowNumber(2);
        row2.setStatus(ImportRowStatus.VALID);
        row2.setNormalizedData(Map.of("productCode", "DUPE-100", "productName", "Second Item"));

        when(importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(importJobId))
                .thenReturn(List.of(row1, row2));

        // Row 1 passes live validation
        when(validationEngine.validate(any())).thenReturn(ValidationResult.success());
        when(recordRepository.save(any(BusinessObjectRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ImportExecutionResponse response = executionService.execute(businessObjectId, importJobId);

        assertEquals(ImportJobStatus.COMPLETED_WITH_ERRORS, response.getStatus());
        assertEquals(1, response.getProcessedRows());
        assertEquals(0, response.getFailedRows());
        assertEquals(1, response.getDuplicateRows());

        assertEquals(ImportRowStatus.IMPORTED, row1.getStatus());
        assertEquals(ImportRowStatus.DUPLICATE, row2.getStatus());

        verify(recordRepository, times(1)).save(any());

        ArgumentCaptor<ConfigurationImportError> errorCaptor =
                ArgumentCaptor.forClass(ConfigurationImportError.class);
        verify(importErrorRepository, times(1)).save(errorCaptor.capture());
        assertEquals("DUPLICATE_VALUE", errorCaptor.getValue().getErrorCode());
        assertTrue(errorCaptor.getValue().getErrorMessage().contains("within the import batch"));
    }

    @Test
    void existingRecordUniquenessConflict() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(1);
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(codeField, nameField));

        ConfigurationImportRow row1 = new ConfigurationImportRow();
        row1.setImportJobId(importJobId);
        row1.setRowNumber(1);
        row1.setStatus(ImportRowStatus.VALID);
        row1.setNormalizedData(Map.of("productCode", "EXISTING-CODE", "productName", "Existing Item"));

        when(importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(importJobId))
                .thenReturn(List.of(row1));

        // Live validation fails because code already exists in DB
        when(validationEngine.validate(any())).thenReturn(ValidationResult.failure(
                List.of(new ValidationError("productCode", "DUPLICATE_VALUE", "Value for field productCode must be unique"))
        ));

        ImportExecutionResponse response = executionService.execute(businessObjectId, importJobId);

        assertEquals(ImportJobStatus.COMPLETED_WITH_ERRORS, response.getStatus());
        assertEquals(0, response.getProcessedRows());
        assertEquals(1, response.getDuplicateRows());
        assertEquals(ImportRowStatus.DUPLICATE, row1.getStatus());

        verify(recordRepository, never()).save(any());
        verify(importErrorRepository, times(1)).save(any());
    }

    @Test
    void concurrentExecutionProtection() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        // Atomic transition returns 0 (indicating another thread already transitioned or updated the status)
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(0);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> executionService.execute(businessObjectId, importJobId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already executing or status was modified concurrently"));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void partialRowFailuresAndAccurateCounters() {
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));
        when(importJobRepository.updateStatusIfStatus(any(), any(), any(), any(), any())).thenReturn(1);
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(codeField, nameField));

        // 3 rows: 1 valid, 1 failing validation, 1 intra-batch duplicate
        ConfigurationImportRow row1 = new ConfigurationImportRow();
        row1.setImportJobId(importJobId);
        row1.setRowNumber(1);
        row1.setStatus(ImportRowStatus.VALID);
        row1.setNormalizedData(Map.of("productCode", "CODE-1", "productName", "Item 1"));

        ConfigurationImportRow row2 = new ConfigurationImportRow();
        row2.setImportJobId(importJobId);
        row2.setRowNumber(2);
        row2.setStatus(ImportRowStatus.VALID);
        row2.setNormalizedData(Map.of("productCode", "CODE-2", "productName", "")); // empty name -> invalid

        ConfigurationImportRow row3 = new ConfigurationImportRow();
        row3.setImportJobId(importJobId);
        row3.setRowNumber(3);
        row3.setStatus(ImportRowStatus.VALID);
        row3.setNormalizedData(Map.of("productCode", "CODE-1", "productName", "Item 3")); // duplicate code of row 1

        when(importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(importJobId))
                .thenReturn(List.of(row1, row2, row3));

        // Row 1 passes
        when(validationEngine.validate(argThat(ctx -> ctx != null && "CODE-1".equals(ctx.values().get("productCode")))))
                .thenReturn(ValidationResult.success());

        // Row 2 fails
        when(validationEngine.validate(argThat(ctx -> ctx != null && "CODE-2".equals(ctx.values().get("productCode")))))
                .thenReturn(ValidationResult.failure(List.of(new ValidationError("productName", "REQUIRED_FIELD", "Required"))));

        when(recordRepository.save(any(BusinessObjectRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ImportExecutionResponse response = executionService.execute(businessObjectId, importJobId);

        assertEquals(ImportJobStatus.COMPLETED_WITH_ERRORS, response.getStatus());
        assertEquals(1, response.getProcessedRows());
        assertEquals(1, response.getFailedRows());
        assertEquals(1, response.getDuplicateRows());
        assertEquals(1, response.getCreatedRecordIds().size());

        assertEquals(ImportRowStatus.IMPORTED, row1.getStatus());
        assertEquals(ImportRowStatus.FAILED, row2.getStatus());
        assertEquals(ImportRowStatus.DUPLICATE, row3.getStatus());

        verify(recordRepository, times(1)).save(any());
        verify(importErrorRepository, times(2)).save(any()); // row 2 format error, row 3 duplicate error
    }

    @Test
    void retrySafetyAndPreventionOfDuplicateRecordCreation() {
        // Case 1: Job already COMPLETED
        job.setStatus(ImportJobStatus.COMPLETED);
        when(importJobRepository.findById(importJobId)).thenReturn(Optional.of(job));

        ResponseStatusException ex1 = assertThrows(
                ResponseStatusException.class,
                () -> executionService.execute(businessObjectId, importJobId)
        );
        assertEquals(HttpStatus.CONFLICT, ex1.getStatusCode());
        assertTrue(ex1.getReason().contains("already been executed"));

        // Case 2: Job already EXECUTING
        job.setStatus(ImportJobStatus.EXECUTING);
        ResponseStatusException ex2 = assertThrows(
                ResponseStatusException.class,
                () -> executionService.execute(businessObjectId, importJobId)
        );
        assertEquals(HttpStatus.CONFLICT, ex2.getStatusCode());
        assertTrue(ex2.getReason().contains("currently executing"));

        verify(recordRepository, never()).save(any());
    }
}
