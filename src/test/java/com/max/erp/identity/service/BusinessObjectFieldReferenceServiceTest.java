package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectFieldReferenceResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldReference;
import com.max.erp.identity.repository.BusinessObjectFieldReferenceRepository;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessObjectFieldReferenceServiceTest {

    private BusinessObjectFieldReferenceRepository referenceRepository;
    private BusinessObjectFieldRepository fieldRepository;
    private BusinessObjectRepository businessObjectRepository;
    private BusinessObjectFieldReferenceService referenceService;

    private UUID businessObjectId;
    private UUID fieldId;
    private UUID targetObjectId;
    private UUID displayFieldId;

    private BusinessObject businessObject;
    private BusinessObject targetObject;
    private BusinessObjectField referenceField;
    private BusinessObjectField displayField;

    @BeforeEach
    void setUp() {
        referenceRepository = mock(BusinessObjectFieldReferenceRepository.class);
        fieldRepository = mock(BusinessObjectFieldRepository.class);
        businessObjectRepository = mock(BusinessObjectRepository.class);

        referenceService = new BusinessObjectFieldReferenceService(
                referenceRepository,
                fieldRepository,
                businessObjectRepository
        );

        businessObjectId = UUID.randomUUID();
        fieldId = UUID.randomUUID();
        targetObjectId = UUID.randomUUID();
        displayFieldId = UUID.randomUUID();

        businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);
        businessObject.setName("Customer");
        businessObject.setStatus("DRAFT");

        targetObject = new BusinessObject();
        targetObject.setId(targetObjectId);
        targetObject.setName("LegalEntity");
        targetObject.setStatus("ACTIVE");

        referenceField = new BusinessObjectField();
        referenceField.setId(fieldId);
        referenceField.setBusinessObject(businessObject);
        referenceField.setFieldKey("legal_entity");
        referenceField.setLabel("Legal Entity");
        referenceField.setFieldType("REFERENCE");

        displayField = new BusinessObjectField();
        displayField.setId(displayFieldId);
        displayField.setBusinessObject(targetObject);
        displayField.setFieldKey("entity_name");
        displayField.setLabel("Entity Name");
        displayField.setFieldType("TEXT");

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(referenceField));
        when(fieldRepository.findById(displayFieldId)).thenReturn(Optional.of(displayField));
        when(businessObjectRepository.findById(targetObjectId)).thenReturn(Optional.of(targetObject));
    }

    @Test
    @DisplayName("getReference returns reference response when found")
    void getReferenceReturnsResponse() {
        BusinessObjectFieldReference reference = createReferenceEntity(
                UUID.randomUUID(), referenceField, targetObject, displayField, false, true
        );

        when(referenceRepository.findByFieldId(fieldId)).thenReturn(Optional.of(reference));

        BusinessObjectFieldReferenceResponse response = referenceService.getReference(businessObjectId, fieldId);

        assertNotNull(response);
        assertEquals(fieldId, response.fieldId());
        assertEquals(targetObjectId, response.targetBusinessObjectId());
        assertEquals(displayFieldId, response.displayFieldId());
        assertFalse(response.allowMultiple());
        assertTrue(response.allowCreate());
    }

    @Test
    @DisplayName("getReference throws 404 when reference config not found")
    void getReferenceThrows404WhenNotFound() {
        when(referenceRepository.findByFieldId(fieldId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.getReference(businessObjectId, fieldId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Reference configuration not found"));
    }

    @Test
    @DisplayName("getReference throws 400 when field belongs to different business object")
    void getReferenceThrows400WhenMismatchedObject() {
        UUID otherObjectId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.getReference(otherObjectId, fieldId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Field does not belong to the specified business object"));
    }

    @Test
    @DisplayName("createReference successfully saves reference with display field")
    void createReferenceSuccess() {
        CreateBusinessObjectFieldReferenceRequest request = new CreateBusinessObjectFieldReferenceRequest(
                targetObjectId,
                displayFieldId,
                false,
                false
        );

        when(referenceRepository.existsByFieldId(fieldId)).thenReturn(false);
        when(referenceRepository.save(any(BusinessObjectFieldReference.class))).thenAnswer(invocation -> {
            BusinessObjectFieldReference saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(saved, "createdAt", OffsetDateTime.now());
            ReflectionTestUtils.setField(saved, "updatedAt", OffsetDateTime.now());
            return saved;
        });

        BusinessObjectFieldReferenceResponse response = referenceService.createReference(
                businessObjectId, fieldId, request
        );

        assertNotNull(response);
        assertEquals(fieldId, response.fieldId());
        assertEquals(targetObjectId, response.targetBusinessObjectId());
        assertEquals(displayFieldId, response.displayFieldId());
        assertFalse(response.allowMultiple());
        assertFalse(response.allowCreate());
    }

    @Test
    @DisplayName("createReference successfully saves reference when display field is null")
    void createReferenceSuccessWithoutDisplayField() {
        CreateBusinessObjectFieldReferenceRequest request = new CreateBusinessObjectFieldReferenceRequest(
                targetObjectId,
                null,
                true,
                true
        );

        when(referenceRepository.existsByFieldId(fieldId)).thenReturn(false);
        when(referenceRepository.save(any(BusinessObjectFieldReference.class))).thenAnswer(invocation -> {
            BusinessObjectFieldReference saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        BusinessObjectFieldReferenceResponse response = referenceService.createReference(
                businessObjectId, fieldId, request
        );

        assertNotNull(response);
        assertNull(response.displayFieldId());
        assertTrue(response.allowMultiple());
        assertTrue(response.allowCreate());
    }

    @Test
    @DisplayName("createReference throws 409 Conflict when business object is not DRAFT")
    void createReferenceRejectsNonDraft() {
        businessObject.setStatus("ACTIVE");

        CreateBusinessObjectFieldReferenceRequest request = new CreateBusinessObjectFieldReferenceRequest(
                targetObjectId, displayFieldId, false, false
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.createReference(businessObjectId, fieldId, request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("while the business object is DRAFT"));
    }

    @Test
    @DisplayName("createReference throws 400 Bad Request when field is not REFERENCE type")
    void createReferenceRejectsNonReferenceField() {
        referenceField.setFieldType("TEXT");

        CreateBusinessObjectFieldReferenceRequest request = new CreateBusinessObjectFieldReferenceRequest(
                targetObjectId, displayFieldId, false, false
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.createReference(businessObjectId, fieldId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("only supported for REFERENCE fields"));
    }

    @Test
    @DisplayName("createReference throws 409 Conflict when configuration already exists")
    void createReferenceRejectsDuplicateConfig() {
        when(referenceRepository.existsByFieldId(fieldId)).thenReturn(true);

        CreateBusinessObjectFieldReferenceRequest request = new CreateBusinessObjectFieldReferenceRequest(
                targetObjectId, displayFieldId, false, false
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.createReference(businessObjectId, fieldId, request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Reference configuration already exists"));
    }

    @Test
    @DisplayName("createReference throws 400 Bad Request when display field belongs to a different object")
    void createReferenceRejectsDisplayFieldFromDifferentObject() {
        BusinessObject thirdObject = new BusinessObject();
        thirdObject.setId(UUID.randomUUID());
        displayField.setBusinessObject(thirdObject);

        CreateBusinessObjectFieldReferenceRequest request = new CreateBusinessObjectFieldReferenceRequest(
                targetObjectId, displayFieldId, false, false
        );

        when(referenceRepository.existsByFieldId(fieldId)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.createReference(businessObjectId, fieldId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Display field must belong to the target business object"));
    }

    @Test
    @DisplayName("updateReference successfully updates configuration")
    void updateReferenceSuccess() {
        UUID referenceId = UUID.randomUUID();
        BusinessObjectFieldReference reference = createReferenceEntity(
                referenceId, referenceField, targetObject, displayField, false, false
        );

        when(referenceRepository.findById(referenceId)).thenReturn(Optional.of(reference));
        when(referenceRepository.save(any(BusinessObjectFieldReference.class))).thenReturn(reference);

        UpdateBusinessObjectFieldReferenceRequest request = new UpdateBusinessObjectFieldReferenceRequest(
                targetObjectId,
                displayFieldId,
                true,
                true
        );

        BusinessObjectFieldReferenceResponse response = referenceService.updateReference(
                businessObjectId, fieldId, referenceId, request
        );

        assertNotNull(response);
        assertTrue(response.allowMultiple());
        assertTrue(response.allowCreate());
    }

    @Test
    @DisplayName("updateReference throws 400 Bad Request when reference does not belong to field")
    void updateReferenceRejectsFieldMismatch() {
        UUID referenceId = UUID.randomUUID();
        BusinessObjectField otherField = new BusinessObjectField();
        otherField.setId(UUID.randomUUID());

        BusinessObjectFieldReference reference = createReferenceEntity(
                referenceId, otherField, targetObject, displayField, false, false
        );

        when(referenceRepository.findById(referenceId)).thenReturn(Optional.of(reference));

        UpdateBusinessObjectFieldReferenceRequest request = new UpdateBusinessObjectFieldReferenceRequest(
                targetObjectId, displayFieldId, false, false
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.updateReference(businessObjectId, fieldId, referenceId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Reference configuration does not belong to this field"));
    }

    @Test
    @DisplayName("deleteReference successfully deletes reference entity")
    void deleteReferenceSuccess() {
        UUID referenceId = UUID.randomUUID();
        BusinessObjectFieldReference reference = createReferenceEntity(
                referenceId, referenceField, targetObject, displayField, false, false
        );

        when(referenceRepository.findById(referenceId)).thenReturn(Optional.of(reference));

        referenceService.deleteReference(businessObjectId, fieldId, referenceId);

        verify(referenceRepository, times(1)).delete(reference);
    }

    @Test
    @DisplayName("deleteReference throws 409 Conflict when business object is not DRAFT")
    void deleteReferenceRejectsNonDraft() {
        businessObject.setStatus("ACTIVE");
        UUID referenceId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                referenceService.deleteReference(businessObjectId, fieldId, referenceId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(referenceRepository, never()).delete(any());
    }

    private BusinessObjectFieldReference createReferenceEntity(
            UUID id,
            BusinessObjectField field,
            BusinessObject targetObj,
            BusinessObjectField dispField,
            boolean allowMultiple,
            boolean allowCreate
    ) {
        BusinessObjectFieldReference ref = new BusinessObjectFieldReference();
        ReflectionTestUtils.setField(ref, "id", id);
        ref.setField(field);
        ref.setTargetBusinessObject(targetObj);
        ref.setDisplayField(dispField);
        ref.setAllowMultiple(allowMultiple);
        ref.setAllowCreate(allowCreate);
        ReflectionTestUtils.setField(ref, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(ref, "updatedAt", OffsetDateTime.now());
        return ref;
    }
}
