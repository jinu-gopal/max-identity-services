package com.max.erp.identity.controller;

import com.max.erp.identity.dto.UpdateBusinessObjectFieldRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.service.*;
import com.max.erp.identity.validation.RecordValidationEngine;
import com.max.erp.identity.validation.RecordValidationException;
import com.max.erp.identity.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConfigurationControllerSecurityTest {

    private BusinessObjectFieldRepository fieldRepository;
    private BusinessObjectRepository businessObjectRepository;
    private BusinessObjectRecordRepository recordRepository;
    private ConfigurationImportJobRepository importJobRepository;
    private RecordValidationEngine validationEngine;
    private BusinessObjectFieldService fieldService;
    private BusinessObjectRecordService recordService;
    private ImportService importService;
    private ImportParserService importParserService;
    private ImportMappingService importMappingService;
    private ImportValidationService importValidationService;

    private MockMvc fieldMockMvc;
    private MockMvc recordMockMvc;
    private MockMvc importMockMvc;

    @BeforeEach
    void setUp() {
        fieldRepository = mock(BusinessObjectFieldRepository.class);
        businessObjectRepository = mock(BusinessObjectRepository.class);
        recordRepository = mock(BusinessObjectRecordRepository.class);
        importJobRepository = mock(ConfigurationImportJobRepository.class);
        validationEngine = mock(RecordValidationEngine.class);

        fieldService = new BusinessObjectFieldService(
                fieldRepository,
                businessObjectRepository
        );

        recordService = new BusinessObjectRecordService(
                recordRepository,
                businessObjectRepository,
                fieldRepository,
                validationEngine
        );

        importService = mock(ImportService.class);
        importParserService = mock(ImportParserService.class);
        importMappingService = mock(ImportMappingService.class);
        importValidationService = mock(ImportValidationService.class);

        BusinessObjectFieldController fieldController =
                new BusinessObjectFieldController(fieldService);

        BusinessObjectRecordController recordController =
                new BusinessObjectRecordController(recordService);

        ImportController importController = new ImportController(
                importService,
                importParserService,
                importJobRepository,
                importMappingService,
                importValidationService
        );

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        fieldMockMvc = MockMvcBuilders
                .standaloneSetup(fieldController)
                .setControllerAdvice(exceptionHandler)
                .build();

        recordMockMvc = MockMvcBuilders
                .standaloneSetup(recordController)
                .setControllerAdvice(exceptionHandler)
                .build();

        importMockMvc = MockMvcBuilders
                .standaloneSetup(importController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    // ============================================================
    // FIELD CONTROLLER SECURITY & CONSISTENCY TESTS
    // ============================================================

    @Test
    void updateFieldShouldReturn400WhenFieldDoesNotBelongToBusinessObject() throws Exception {
        UUID parentObjectId = UUID.randomUUID();
        UUID otherObjectId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        BusinessObject otherObject = new BusinessObject();
        otherObject.setId(otherObjectId);
        otherObject.setStatus("DRAFT");

        BusinessObjectField field = new BusinessObjectField();
        field.setId(fieldId);
        field.setBusinessObject(otherObject);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        fieldMockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}",
                parentObjectId, fieldId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "Updated Label"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Field does not belong to the specified business object"));
    }

    @Test
    void updateFieldShouldReturn409ConflictWhenBusinessObjectIsNotDraft() throws Exception {
        UUID businessObjectId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        BusinessObject publishedObject = new BusinessObject();
        publishedObject.setId(businessObjectId);
        publishedObject.setStatus("PUBLISHED");

        BusinessObjectField field = new BusinessObjectField();
        field.setId(fieldId);
        field.setBusinessObject(publishedObject);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        fieldMockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}",
                businessObjectId, fieldId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "Updated Label"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Fields can only be edited while the business object is in DRAFT status"));
    }

    @Test
    void deleteFieldShouldReturn409ConflictWhenBusinessObjectIsNotDraft() throws Exception {
        UUID businessObjectId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        BusinessObject archivedObject = new BusinessObject();
        archivedObject.setId(businessObjectId);
        archivedObject.setStatus("ARCHIVED");

        BusinessObjectField field = new BusinessObjectField();
        field.setId(fieldId);
        field.setBusinessObject(archivedObject);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        fieldMockMvc.perform(delete("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}",
                businessObjectId, fieldId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Fields can only be deleted while the business object is in DRAFT status"));
    }

    // ============================================================
    // RECORD CONTROLLER SECURITY & VALIDATION TESTS
    // ============================================================

    @Test
    void getRecordShouldReturn404WhenRecordDoesNotBelongToBusinessObject() throws Exception {
        UUID requestedObjectId = UUID.randomUUID();
        UUID actualObjectId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();

        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setBusinessObjectId(actualObjectId);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        recordMockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/records/{recordId}",
                requestedObjectId, recordId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createRecordShouldPropagateStructuredValidationErrors() throws Exception {
        UUID businessObjectId = UUID.randomUUID();

        BusinessObject businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);
        businessObject.setStatus("PUBLISHED");

        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of());

        ValidationError error1 = new ValidationError("code", "REQUIRED", "Field is required");
        ValidationError error2 = new ValidationError("email", "INVALID_FORMAT", "Invalid email format");

        doThrow(new RecordValidationException(List.of(error1, error2)))
                .when(validationEngine).validate(any());

        recordMockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/records",
                businessObjectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.code").value("Field is required"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Invalid email format"));
    }

    // ============================================================
    // IMPORT CONTROLLER SECURITY TESTS
    // ============================================================

    @Test
    void parseImportShouldReturn400WhenImportJobBelongsToDifferentBusinessObject() throws Exception {
        UUID businessObjectId = UUID.randomUUID();
        UUID otherObjectId = UUID.randomUUID();
        UUID importId = UUID.randomUUID();

        ConfigurationImportJob job = mock(ConfigurationImportJob.class);
        when(job.getId()).thenReturn(importId);
        when(job.getBusinessObjectId()).thenReturn(otherObjectId);

        when(importJobRepository.findById(importId)).thenReturn(Optional.of(job));

        importMockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/imports/{importId}/parse",
                businessObjectId, importId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Import job does not belong to the specified business object"));
    }
}
