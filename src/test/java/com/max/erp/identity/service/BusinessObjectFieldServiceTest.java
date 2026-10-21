package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectFieldResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessObjectFieldServiceTest {

    private BusinessObjectFieldRepository fieldRepository;
    private BusinessObjectRepository businessObjectRepository;
    private BusinessObjectFieldService fieldService;

    private UUID businessObjectId;
    private UUID fieldId;
    private BusinessObject businessObject;

    @BeforeEach
    void setUp() {
        fieldRepository = mock(BusinessObjectFieldRepository.class);
        businessObjectRepository = mock(BusinessObjectRepository.class);
        fieldService = new BusinessObjectFieldService(fieldRepository, businessObjectRepository);

        businessObjectId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);
        businessObject.setName("Customer");
        businessObject.setStatus("DRAFT");
    }

    @Test
    @DisplayName("getFields returns ordered list of fields for business object")
    void getFieldsReturnsList() {
        when(businessObjectRepository.existsById(businessObjectId)).thenReturn(true);

        BusinessObjectField f1 = createFieldEntity(UUID.randomUUID(), "name", "Customer Name", "TEXT", 1);
        BusinessObjectField f2 = createFieldEntity(UUID.randomUUID(), "credit_limit", "Credit Limit", "CURRENCY", 2);

        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId))
                .thenReturn(List.of(f1, f2));

        List<BusinessObjectFieldResponse> result = fieldService.getFields(businessObjectId);

        assertEquals(2, result.size());
        assertEquals("name", result.get(0).fieldKey());
        assertEquals("credit_limit", result.get(1).fieldKey());
    }

    @Test
    @DisplayName("getFields throws IllegalArgumentException when business object not found")
    void getFieldsThrowsWhenObjectNotFound() {
        when(businessObjectRepository.existsById(businessObjectId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fieldService.getFields(businessObjectId)
        );

        assertTrue(ex.getMessage().contains("Business object not found"));
    }

    @Test
    @DisplayName("createField successfully creates field in DRAFT object")
    void createFieldSuccess() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.existsByBusinessObjectIdAndFieldKey(businessObjectId, "credit_limit")).thenReturn(false);

        CreateBusinessObjectFieldRequest request = new CreateBusinessObjectFieldRequest(
                "credit_limit",
                "Credit Limit",
                "Maximum credit allowed",
                "CURRENCY",
                false,
                false,
                false,
                false,
                "0",
                new BigDecimal("0"),
                new BigDecimal("50000"),
                null,
                1
        );

        when(fieldRepository.save(any(BusinessObjectField.class))).thenAnswer(invocation -> {
            BusinessObjectField saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", fieldId);
            ReflectionTestUtils.setField(saved, "createdAt", OffsetDateTime.now());
            ReflectionTestUtils.setField(saved, "updatedAt", OffsetDateTime.now());
            return saved;
        });

        BusinessObjectFieldResponse response = fieldService.createField(businessObjectId, request);

        assertNotNull(response);
        assertEquals(fieldId, response.id());
        assertEquals("credit_limit", response.fieldKey());
        assertEquals("Credit Limit", response.label());
        assertEquals("CURRENCY", response.fieldType());
        assertEquals(new BigDecimal("0"), response.minValue());
        assertEquals(new BigDecimal("50000"), response.maxValue());
    }

    @Test
    @DisplayName("createField normalizes fieldKey to lowercase")
    void createFieldNormalizesKey() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.existsByBusinessObjectIdAndFieldKey(businessObjectId, "email_address")).thenReturn(false);

        CreateBusinessObjectFieldRequest request = new CreateBusinessObjectFieldRequest(
                "  Email_Address  ",
                "Email",
                null,
                "EMAIL",
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                2
        );

        when(fieldRepository.save(any(BusinessObjectField.class))).thenAnswer(invocation -> {
            BusinessObjectField saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", fieldId);
            return saved;
        });

        BusinessObjectFieldResponse response = fieldService.createField(businessObjectId, request);

        assertEquals("email_address", response.fieldKey());
    }

    @Test
    @DisplayName("createField throws IllegalStateException when object is not DRAFT")
    void createFieldRejectsNonDraft() {
        businessObject.setStatus("ACTIVE");
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));

        CreateBusinessObjectFieldRequest request = new CreateBusinessObjectFieldRequest(
                "notes", "Notes", null, "TEXT", false, false, false, false, null, null, null, null, 1
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fieldService.createField(businessObjectId, request)
        );

        assertTrue(ex.getMessage().contains("while the business object is in DRAFT status"));
    }

    @Test
    @DisplayName("createField throws IllegalArgumentException on duplicate fieldKey")
    void createFieldRejectsDuplicateKey() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.existsByBusinessObjectIdAndFieldKey(businessObjectId, "existing_key")).thenReturn(true);

        CreateBusinessObjectFieldRequest request = new CreateBusinessObjectFieldRequest(
                "existing_key", "Label", null, "TEXT", false, false, false, false, null, null, null, null, 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fieldService.createField(businessObjectId, request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("createField throws IllegalArgumentException for unsupported field type")
    void createFieldRejectsUnsupportedType() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.existsByBusinessObjectIdAndFieldKey(businessObjectId, "bad_field")).thenReturn(false);

        CreateBusinessObjectFieldRequest request = new CreateBusinessObjectFieldRequest(
                "bad_field", "Label", null, "UNSUPPORTED_TYPE", false, false, false, false, null, null, null, null, 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fieldService.createField(businessObjectId, request)
        );

        assertTrue(ex.getMessage().contains("Unsupported field type"));
    }

    @Test
    @DisplayName("createField throws IllegalArgumentException when min > max")
    void createFieldRejectsMinGreaterThanMax() {
        when(businessObjectRepository.findById(businessObjectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.existsByBusinessObjectIdAndFieldKey(businessObjectId, "qty")).thenReturn(false);

        CreateBusinessObjectFieldRequest request = new CreateBusinessObjectFieldRequest(
                "qty", "Quantity", null, "NUMBER", false, false, false, false, null,
                new BigDecimal("100"), new BigDecimal("10"), null, 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fieldService.createField(businessObjectId, request)
        );

        assertTrue(ex.getMessage().contains("Minimum value cannot be greater than maximum value"));
    }

    @Test
    @DisplayName("updateField successfully modifies field in DRAFT object")
    void updateFieldSuccess() {
        BusinessObjectField field = createFieldEntity(fieldId, "rating", "Rating", "NUMBER", 1);
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(fieldRepository.save(any(BusinessObjectField.class))).thenReturn(field);

        UpdateBusinessObjectFieldRequest request = new UpdateBusinessObjectFieldRequest();
        request.setLabel("Customer Rating");
        request.setRequired(true);
        request.setMinValue(new BigDecimal("1"));
        request.setMaxValue(new BigDecimal("5"));

        BusinessObjectFieldResponse response = fieldService.updateField(businessObjectId, fieldId, request);

        assertNotNull(response);
        assertEquals("Customer Rating", response.label());
        assertTrue(response.required());
    }

    @Test
    @DisplayName("updateField throws IllegalArgumentException when field belongs to different business object")
    void updateFieldRejectsMismatchedBusinessObject() {
        BusinessObject otherObject = new BusinessObject();
        otherObject.setId(UUID.randomUUID());
        otherObject.setStatus("DRAFT");

        BusinessObjectField field = createFieldEntity(fieldId, "f1", "Field", "TEXT", 1);
        field.setBusinessObject(otherObject);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        UpdateBusinessObjectFieldRequest request = new UpdateBusinessObjectFieldRequest();
        request.setLabel("New Label");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fieldService.updateField(businessObjectId, fieldId, request)
        );

        assertTrue(ex.getMessage().contains("Field does not belong to the specified business object"));
    }

    @Test
    @DisplayName("updateField throws IllegalStateException when object is not DRAFT")
    void updateFieldRejectsNonDraft() {
        businessObject.setStatus("PUBLISHED");
        BusinessObjectField field = createFieldEntity(fieldId, "f1", "Field", "TEXT", 1);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        UpdateBusinessObjectFieldRequest request = new UpdateBusinessObjectFieldRequest();
        request.setLabel("New Label");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fieldService.updateField(businessObjectId, fieldId, request)
        );

        assertTrue(ex.getMessage().contains("while the business object is in DRAFT status"));
    }

    @Test
    @DisplayName("deleteField successfully removes field in DRAFT object")
    void deleteFieldSuccess() {
        BusinessObjectField field = createFieldEntity(fieldId, "temp_field", "Temp", "TEXT", 1);
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        fieldService.deleteField(businessObjectId, fieldId);

        verify(fieldRepository, times(1)).delete(field);
    }

    @Test
    @DisplayName("deleteField throws IllegalStateException when object is not DRAFT")
    void deleteFieldRejectsNonDraft() {
        businessObject.setStatus("ACTIVE");
        BusinessObjectField field = createFieldEntity(fieldId, "temp_field", "Temp", "TEXT", 1);
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                fieldService.deleteField(businessObjectId, fieldId)
        );

        assertTrue(ex.getMessage().contains("while the business object is in DRAFT status"));
        verify(fieldRepository, never()).delete(any());
    }

    private BusinessObjectField createFieldEntity(
            UUID id,
            String key,
            String label,
            String type,
            int order
    ) {
        BusinessObjectField f = new BusinessObjectField();
        ReflectionTestUtils.setField(f, "id", id);
        f.setBusinessObject(businessObject);
        f.setFieldKey(key);
        f.setLabel(label);
        f.setFieldType(type);
        f.setDisplayOrder(order);
        f.setRequired(false);
        f.setUniqueValue(false);
        f.setReadOnly(false);
        f.setHidden(false);
        ReflectionTestUtils.setField(f, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(f, "updatedAt", OffsetDateTime.now());
        return f;
    }
}
