package com.max.erp.identity.service;

import com.max.erp.identity.dto.BusinessObjectResponse;
import com.max.erp.identity.dto.CreateBusinessObjectRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectRequest;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.repository.BusinessObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessObjectServiceTest {

    private BusinessObjectRepository businessObjectRepository;
    private com.max.erp.identity.repository.BusinessObjectFieldRepository fieldRepository;
    private BusinessObjectService businessObjectService;

    private UUID objectId;
    private BusinessObject businessObject;

    @BeforeEach
    void setUp() {
        businessObjectRepository = mock(BusinessObjectRepository.class);
        fieldRepository = mock(com.max.erp.identity.repository.BusinessObjectFieldRepository.class);
        businessObjectService = new BusinessObjectService(businessObjectRepository, fieldRepository);

        objectId = UUID.randomUUID();

        businessObject = new BusinessObject();
        businessObject.setId(objectId);
        businessObject.setObjectKey("customer");
        businessObject.setName("Customer");
        businessObject.setDescription("Customer entity");
        businessObject.setStatus("DRAFT");
        businessObject.setVersionNumber(1);
        ReflectionTestUtils.setField(businessObject, "createdAt", Instant.now());
        ReflectionTestUtils.setField(businessObject, "updatedAt", Instant.now());
    }

    @Test
    @DisplayName("getAll returns all business objects ordered by name")
    void getAllReturnsList() {
        BusinessObject obj2 = new BusinessObject();
        obj2.setId(UUID.randomUUID());
        obj2.setName("Vendor");
        obj2.setObjectKey("vendor");
        obj2.setStatus("ACTIVE");

        when(businessObjectRepository.findAllByOrderByNameAsc()).thenReturn(List.of(businessObject, obj2));

        List<BusinessObjectResponse> result = businessObjectService.getAll();

        assertEquals(2, result.size());
        assertEquals("Customer", result.get(0).getName());
        assertEquals("Vendor", result.get(1).getName());
    }

    @Test
    @DisplayName("getById returns business object when found")
    void getByIdReturnsObject() {
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.of(businessObject));

        BusinessObjectResponse response = businessObjectService.getById(objectId);

        assertNotNull(response);
        assertEquals(objectId, response.getId());
        assertEquals("customer", response.getObjectKey());
        assertEquals("Customer", response.getName());
        assertEquals("DRAFT", response.getStatus());
    }

    @Test
    @DisplayName("getById throws 404 NOT_FOUND when object not found")
    void getByIdThrows404WhenNotFound() {
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                businessObjectService.getById(objectId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Business object not found"));
    }

    @Test
    @DisplayName("create successfully creates new business object with status DRAFT")
    void createSuccess() {
        CreateBusinessObjectRequest request = new CreateBusinessObjectRequest();
        request.setObjectKey("invoice");
        request.setName("Invoice");
        request.setDescription("Invoices and billing");

        when(businessObjectRepository.existsByObjectKey("invoice")).thenReturn(false);
        when(businessObjectRepository.save(any(BusinessObject.class))).thenAnswer(inv -> {
            BusinessObject saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        BusinessObjectResponse response = businessObjectService.create(request);

        assertNotNull(response);
        assertEquals("invoice", response.getObjectKey());
        assertEquals("Invoice", response.getName());
        assertEquals("DRAFT", response.getStatus());
        assertEquals(1, response.getVersionNumber());
    }

    @Test
    @DisplayName("create throws IllegalArgumentException on duplicate objectKey")
    void createRejectsDuplicateKey() {
        CreateBusinessObjectRequest request = new CreateBusinessObjectRequest();
        request.setObjectKey("customer");
        request.setName("Customer Duplicate");

        when(businessObjectRepository.existsByObjectKey("customer")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                businessObjectService.create(request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(businessObjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("update successfully updates DRAFT business object")
    void updateSuccess() {
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.of(businessObject));
        when(businessObjectRepository.save(any(BusinessObject.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBusinessObjectRequest request = new UpdateBusinessObjectRequest();
        request.setName("Updated Customer");
        request.setDescription("Updated description");

        BusinessObjectResponse response = businessObjectService.update(objectId, request);

        assertNotNull(response);
        assertEquals("Updated Customer", response.getName());
        assertEquals("Updated description", response.getDescription());
    }

    @Test
    @DisplayName("update throws IllegalStateException when object is not DRAFT")
    void updateRejectsNonDraft() {
        businessObject.setStatus("PUBLISHED");
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.of(businessObject));

        UpdateBusinessObjectRequest request = new UpdateBusinessObjectRequest();
        request.setName("New Name");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                businessObjectService.update(objectId, request)
        );

        assertTrue(ex.getMessage().contains("Only DRAFT business objects can be updated"));
        verify(businessObjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("publish successfully transitions DRAFT business object with fields to PUBLISHED")
    void publishSuccess() {
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.of(businessObject));
        when(businessObjectRepository.save(any(BusinessObject.class))).thenAnswer(inv -> inv.getArgument(0));

        com.max.erp.identity.entity.BusinessObjectField field = new com.max.erp.identity.entity.BusinessObjectField();
        field.setId(UUID.randomUUID());
        field.setFieldKey("name");
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(objectId)).thenReturn(List.of(field));

        BusinessObjectResponse response = businessObjectService.publish(objectId);

        assertNotNull(response);
        assertEquals("PUBLISHED", response.getStatus());
        verify(businessObjectRepository, times(1)).save(businessObject);
    }

    @Test
    @DisplayName("publish throws 409 Conflict when business object is not DRAFT")
    void publishRejectsNonDraft() {
        businessObject.setStatus("PUBLISHED");
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.of(businessObject));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                businessObjectService.publish(objectId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Only DRAFT business objects can be published"));
        verify(businessObjectRepository, never()).save(any());
    }

    @Test
    @DisplayName("publish throws 400 Bad Request when business object has no configured fields")
    void publishRejectsNoFields() {
        when(businessObjectRepository.findById(objectId)).thenReturn(Optional.of(businessObject));
        when(fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(objectId)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                businessObjectService.publish(objectId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("without any configured fields"));
        verify(businessObjectRepository, never()).save(any());
    }
}
