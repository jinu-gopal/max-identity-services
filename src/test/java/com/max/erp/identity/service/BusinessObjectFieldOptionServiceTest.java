package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectFieldOptionResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldOption;
import com.max.erp.identity.repository.BusinessObjectFieldOptionRepository;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessObjectFieldOptionServiceTest {

    private BusinessObjectFieldOptionRepository optionRepository;
    private BusinessObjectFieldRepository fieldRepository;
    private BusinessObjectFieldOptionService optionService;

    private UUID businessObjectId;
    private UUID fieldId;
    private BusinessObject businessObject;
    private BusinessObjectField field;

    @BeforeEach
    void setUp() {
        optionRepository = mock(BusinessObjectFieldOptionRepository.class);
        fieldRepository = mock(BusinessObjectFieldRepository.class);
        optionService = new BusinessObjectFieldOptionService(optionRepository, fieldRepository);

        businessObjectId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        businessObject = new BusinessObject();
        businessObject.setId(businessObjectId);
        businessObject.setName("Customer");
        businessObject.setStatus("DRAFT");

        field = new BusinessObjectField();
        field.setId(fieldId);
        field.setBusinessObject(businessObject);
        field.setFieldKey("customer_type");
        field.setLabel("Customer Type");
        field.setFieldType("CHOICE");

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
    }

    @Test
    @DisplayName("getOptions returns all options for choice field ordered by display order")
    void getOptionsReturnsOrderedList() {
        BusinessObjectFieldOption opt1 = createOptionEntity(UUID.randomUUID(), "Corporate", "Corporate", 0, true, false);
        BusinessObjectFieldOption opt2 = createOptionEntity(UUID.randomUUID(), "Individual", "Individual", 1, true, false);

        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId))
                .thenReturn(List.of(opt1, opt2));

        List<BusinessObjectFieldOptionResponse> responses = optionService.getOptions(businessObjectId, fieldId);

        assertEquals(2, responses.size());
        assertEquals("Corporate", responses.get(0).optionValue());
        assertEquals("Individual", responses.get(1).optionValue());
    }

    @Test
    @DisplayName("createOption successfully saves new choice option in draft object")
    void createOptionSuccess() {
        CreateBusinessObjectFieldOptionRequest request = new CreateBusinessObjectFieldOptionRequest(
                "Business",
                "Business Account",
                2,
                true,
                false
        );

        when(optionRepository.existsByFieldIdAndOptionValue(fieldId, "Business")).thenReturn(false);
        when(optionRepository.save(any(BusinessObjectFieldOption.class))).thenAnswer(invocation -> {
            BusinessObjectFieldOption saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(saved, "createdAt", OffsetDateTime.now());
            ReflectionTestUtils.setField(saved, "updatedAt", OffsetDateTime.now());
            return saved;
        });

        BusinessObjectFieldOptionResponse response = optionService.createOption(businessObjectId, fieldId, request);

        assertNotNull(response);
        assertEquals("Business", response.optionValue());
        assertEquals("Business Account", response.optionLabel());
        assertEquals(2, response.displayOrder());
        assertTrue(response.active());
        assertFalse(response.defaultOption());

        verify(optionRepository, times(1)).save(any(BusinessObjectFieldOption.class));
    }

    @Test
    @DisplayName("createOption rejects modification when business object is not in DRAFT status")
    void createOptionRejectsNonDraft() {
        businessObject.setStatus("PUBLISHED");

        CreateBusinessObjectFieldOptionRequest request = new CreateBusinessObjectFieldOptionRequest(
                "Business",
                "Business Account",
                2,
                true,
                false
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> optionService.createOption(businessObjectId, fieldId, request));

        assertTrue(ex.getMessage().contains("DRAFT"));
        verify(optionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOption rejects options for non-choice field types")
    void createOptionRejectsNonChoiceField() {
        field.setFieldType("TEXT");

        CreateBusinessObjectFieldOptionRequest request = new CreateBusinessObjectFieldOptionRequest(
                "Opt1",
                "Option 1",
                0,
                true,
                false
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> optionService.createOption(businessObjectId, fieldId, request));

        assertTrue(ex.getMessage().contains("CHOICE"));
        verify(optionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOption rejects blank option value or label")
    void createOptionRejectsBlankValues() {
        CreateBusinessObjectFieldOptionRequest blankValueReq = new CreateBusinessObjectFieldOptionRequest(
                "   ",
                "Label",
                0,
                true,
                false
        );

        assertThrows(IllegalArgumentException.class,
                () -> optionService.createOption(businessObjectId, fieldId, blankValueReq));

        CreateBusinessObjectFieldOptionRequest blankLabelReq = new CreateBusinessObjectFieldOptionRequest(
                "Val",
                "   ",
                0,
                true,
                false
        );

        assertThrows(IllegalArgumentException.class,
                () -> optionService.createOption(businessObjectId, fieldId, blankLabelReq));
    }

    @Test
    @DisplayName("createOption rejects duplicate option value on same field")
    void createOptionRejectsDuplicateValue() {
        CreateBusinessObjectFieldOptionRequest request = new CreateBusinessObjectFieldOptionRequest(
                "Corporate",
                "Corporate Account",
                0,
                true,
                false
        );

        when(optionRepository.existsByFieldIdAndOptionValue(fieldId, "Corporate")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> optionService.createOption(businessObjectId, fieldId, request));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(optionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOption clears previous default option when new default option is added")
    void createOptionClearsPreviousDefault() {
        BusinessObjectFieldOption existingDefault = createOptionEntity(UUID.randomUUID(), "Corporate", "Corporate", 0, true, true);
        when(optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId)).thenReturn(List.of(existingDefault));

        CreateBusinessObjectFieldOptionRequest request = new CreateBusinessObjectFieldOptionRequest(
                "Individual",
                "Individual",
                1,
                true,
                true
        );

        when(optionRepository.existsByFieldIdAndOptionValue(fieldId, "Individual")).thenReturn(false);
        when(optionRepository.save(any(BusinessObjectFieldOption.class))).thenAnswer(invocation -> invocation.getArgument(0));

        optionService.createOption(businessObjectId, fieldId, request);

        assertFalse(existingDefault.isDefaultOption());
        verify(optionRepository, times(1)).save(any(BusinessObjectFieldOption.class));
    }

    @Test
    @DisplayName("createOption rejects setting inactive option as default")
    void createOptionRejectsInactiveDefault() {
        CreateBusinessObjectFieldOptionRequest request = new CreateBusinessObjectFieldOptionRequest(
                "ArchivedOpt",
                "Archived Option",
                0,
                false,
                true
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> optionService.createOption(businessObjectId, fieldId, request));

        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("updateOption updates fields and persists changes")
    void updateOptionSuccess() {
        UUID optionId = UUID.randomUUID();
        BusinessObjectFieldOption existing = createOptionEntity(optionId, "OldVal", "OldLabel", 0, true, false);

        when(optionRepository.findById(optionId)).thenReturn(Optional.of(existing));
        when(optionRepository.existsByFieldIdAndOptionValue(fieldId, "NewVal")).thenReturn(false);
        when(optionRepository.save(any(BusinessObjectFieldOption.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBusinessObjectFieldOptionRequest updateReq = new UpdateBusinessObjectFieldOptionRequest();
        updateReq.setOptionValue("NewVal");
        updateReq.setOptionLabel("New Label");
        updateReq.setDisplayOrder(5);
        updateReq.setActive(true);

        BusinessObjectFieldOptionResponse response = optionService.updateOption(businessObjectId, fieldId, optionId, updateReq);

        assertEquals("NewVal", response.optionValue());
        assertEquals("New Label", response.optionLabel());
        assertEquals(5, response.displayOrder());
        verify(optionRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("deleteOption removes existing option from repository")
    void deleteOptionSuccess() {
        UUID optionId = UUID.randomUUID();
        BusinessObjectFieldOption existing = createOptionEntity(optionId, "Val", "Label", 0, true, false);

        when(optionRepository.findById(optionId)).thenReturn(Optional.of(existing));

        optionService.deleteOption(businessObjectId, fieldId, optionId);

        verify(optionRepository, times(1)).delete(existing);
    }

    @Test
    @DisplayName("deleteOption rejects option belonging to another field")
    void deleteOptionRejectsFieldMismatch() {
        UUID optionId = UUID.randomUUID();
        BusinessObjectField otherField = new BusinessObjectField();
        otherField.setId(UUID.randomUUID());

        BusinessObjectFieldOption existing = createOptionEntity(optionId, "Val", "Label", 0, true, false);
        existing.setField(otherField);

        when(optionRepository.findById(optionId)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> optionService.deleteOption(businessObjectId, fieldId, optionId));

        assertTrue(ex.getMessage().contains("does not belong to the specified field"));
        verify(optionRepository, never()).delete(any());
    }

    private BusinessObjectFieldOption createOptionEntity(UUID id, String value, String label, int order, boolean active, boolean isDefault) {
        BusinessObjectFieldOption opt = new BusinessObjectFieldOption();
        ReflectionTestUtils.setField(opt, "id", id);
        opt.setField(field);
        opt.setOptionValue(value);
        opt.setOptionLabel(label);
        opt.setDisplayOrder(order);
        opt.setActive(active);
        opt.setDefaultOption(isDefault);
        ReflectionTestUtils.setField(opt, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(opt, "updatedAt", OffsetDateTime.now());
        return opt;
    }
}
