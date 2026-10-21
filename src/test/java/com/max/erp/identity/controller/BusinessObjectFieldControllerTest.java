package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectFieldResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldRequest;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.BusinessObjectFieldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessObjectFieldControllerTest {

    private BusinessObjectFieldService fieldService;
    private MockMvc mockMvc;

    private UUID businessObjectId;
    private UUID fieldId;

    @BeforeEach
    void setUp() {
        fieldService = mock(BusinessObjectFieldService.class);
        BusinessObjectFieldController controller = new BusinessObjectFieldController(fieldService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        businessObjectId = UUID.randomUUID();
        fieldId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET fields returns 200 with field array")
    void getFieldsReturns200() throws Exception {
        BusinessObjectFieldResponse response = new BusinessObjectFieldResponse(
                fieldId,
                businessObjectId,
                "first_name",
                "First Name",
                "Given name",
                "TEXT",
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                0,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(fieldService.getFields(businessObjectId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/fields", businessObjectId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(fieldId.toString()))
                .andExpect(jsonPath("$[0].fieldKey").value("first_name"))
                .andExpect(jsonPath("$[0].label").value("First Name"));

        verify(fieldService, times(1)).getFields(businessObjectId);
    }

    @Test
    @DisplayName("POST createField returns 200 with created field")
    void createFieldReturns200() throws Exception {
        BusinessObjectFieldResponse response = new BusinessObjectFieldResponse(
                fieldId,
                businessObjectId,
                "credit_limit",
                "Credit Limit",
                null,
                "CURRENCY",
                false,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                1,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(fieldService.createField(eq(businessObjectId), any(CreateBusinessObjectFieldRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/fields", businessObjectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "fieldKey": "credit_limit",
                            "label": "Credit Limit",
                            "fieldType": "CURRENCY",
                            "required": false,
                            "uniqueValue": false,
                            "readOnly": false,
                            "hidden": false,
                            "displayOrder": 1
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fieldId.toString()))
                .andExpect(jsonPath("$.fieldKey").value("credit_limit"))
                .andExpect(jsonPath("$.fieldType").value("CURRENCY"));

        verify(fieldService, times(1)).createField(eq(businessObjectId), any());
    }

    @Test
    @DisplayName("PUT updateField returns 200 with updated field")
    void updateFieldReturns200() throws Exception {
        BusinessObjectFieldResponse response = new BusinessObjectFieldResponse(
                fieldId,
                businessObjectId,
                "credit_limit",
                "Updated Credit Limit",
                null,
                "CURRENCY",
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                1,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(fieldService.updateField(eq(businessObjectId), eq(fieldId), any(UpdateBusinessObjectFieldRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}",
                businessObjectId, fieldId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "label": "Updated Credit Limit",
                            "required": true
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Updated Credit Limit"))
                .andExpect(jsonPath("$.required").value(true));

        verify(fieldService, times(1)).updateField(eq(businessObjectId), eq(fieldId), any());
    }

    @Test
    @DisplayName("DELETE deleteField returns 204 No Content")
    void deleteFieldReturnsNoContent() throws Exception {
        doNothing().when(fieldService).deleteField(businessObjectId, fieldId);

        mockMvc.perform(delete("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}",
                businessObjectId, fieldId))
                .andExpect(status().isNoContent());

        verify(fieldService, times(1)).deleteField(businessObjectId, fieldId);
    }
}
