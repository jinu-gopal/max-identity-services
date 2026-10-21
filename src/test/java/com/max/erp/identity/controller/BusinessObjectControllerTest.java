package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectResponse;
import com.max.erp.identity.dto.CreateBusinessObjectRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectRequest;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.BusinessObjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessObjectControllerTest {

    private BusinessObjectService businessObjectService;
    private MockMvc mockMvc;

    private UUID objectId;

    @BeforeEach
    void setUp() {
        businessObjectService = mock(BusinessObjectService.class);
        BusinessObjectController controller = new BusinessObjectController(businessObjectService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET business objects returns 200 with object list")
    void getAllReturns200() throws Exception {
        BusinessObjectResponse response = new BusinessObjectResponse();
        response.setId(objectId);
        response.setObjectKey("customer");
        response.setName("Customer");
        response.setStatus("DRAFT");

        when(businessObjectService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/configuration/business-objects")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(objectId.toString()))
                .andExpect(jsonPath("$[0].objectKey").value("customer"))
                .andExpect(jsonPath("$[0].name").value("Customer"));

        verify(businessObjectService, times(1)).getAll();
    }

    @Test
    @DisplayName("GET single business object returns 200 with object response")
    void getByIdReturns200() throws Exception {
        BusinessObjectResponse response = new BusinessObjectResponse();
        response.setId(objectId);
        response.setObjectKey("customer");
        response.setName("Customer");
        response.setStatus("DRAFT");

        when(businessObjectService.getById(objectId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/configuration/business-objects/{id}", objectId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(objectId.toString()))
                .andExpect(jsonPath("$.objectKey").value("customer"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(businessObjectService, times(1)).getById(objectId);
    }

    @Test
    @DisplayName("POST create business object returns 201 CREATED")
    void createReturns201() throws Exception {
        BusinessObjectResponse response = new BusinessObjectResponse();
        response.setId(objectId);
        response.setObjectKey("vendor");
        response.setName("Vendor");
        response.setStatus("DRAFT");
        response.setVersionNumber(1);

        when(businessObjectService.create(any(CreateBusinessObjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/configuration/business-objects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "objectKey": "vendor",
                            "name": "Vendor",
                            "description": "Vendor profiles"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(objectId.toString()))
                .andExpect(jsonPath("$.objectKey").value("vendor"))
                .andExpect(jsonPath("$.name").value("Vendor"));

        verify(businessObjectService, times(1)).create(any());
    }

    @Test
    @DisplayName("PUT update business object returns 200 OK")
    void updateReturns200() throws Exception {
        BusinessObjectResponse response = new BusinessObjectResponse();
        response.setId(objectId);
        response.setObjectKey("customer");
        response.setName("Updated Customer");
        response.setStatus("DRAFT");

        when(businessObjectService.update(eq(objectId), any(UpdateBusinessObjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/configuration/business-objects/{id}", objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Updated Customer",
                            "description": "Updated Customer description"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Customer"));

        verify(businessObjectService, times(1)).update(eq(objectId), any());
    }

    @Test
    @DisplayName("POST publish business object returns 200 OK with PUBLISHED status")
    void publishReturns200() throws Exception {
        BusinessObjectResponse response = new BusinessObjectResponse();
        response.setId(objectId);
        response.setObjectKey("customer");
        response.setName("Customer");
        response.setStatus("PUBLISHED");

        when(businessObjectService.publish(objectId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{id}/publish", objectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(objectId.toString()))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(businessObjectService, times(1)).publish(objectId);
    }
}
