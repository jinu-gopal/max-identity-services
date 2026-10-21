package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectFieldReferenceResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldReferenceRequest;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.BusinessObjectFieldReferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessObjectFieldReferenceControllerTest {

    private BusinessObjectFieldReferenceService referenceService;
    private MockMvc mockMvc;

    private UUID businessObjectId;
    private UUID fieldId;
    private UUID referenceId;
    private UUID targetObjectId;
    private UUID displayFieldId;

    @BeforeEach
    void setUp() {
        referenceService = mock(BusinessObjectFieldReferenceService.class);
        BusinessObjectFieldReferenceController controller = new BusinessObjectFieldReferenceController(referenceService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        businessObjectId = UUID.randomUUID();
        fieldId = UUID.randomUUID();
        referenceId = UUID.randomUUID();
        targetObjectId = UUID.randomUUID();
        displayFieldId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET reference returns 200 with reference response")
    void getReferenceReturns200() throws Exception {
        BusinessObjectFieldReferenceResponse response = new BusinessObjectFieldReferenceResponse(
                referenceId,
                fieldId,
                targetObjectId,
                displayFieldId,
                false,
                true
        );

        when(referenceService.getReference(businessObjectId, fieldId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/reference",
                businessObjectId, fieldId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(referenceId.toString()))
                .andExpect(jsonPath("$.fieldId").value(fieldId.toString()))
                .andExpect(jsonPath("$.targetBusinessObjectId").value(targetObjectId.toString()))
                .andExpect(jsonPath("$.displayFieldId").value(displayFieldId.toString()))
                .andExpect(jsonPath("$.allowMultiple").value(false))
                .andExpect(jsonPath("$.allowCreate").value(true));

        verify(referenceService, times(1)).getReference(businessObjectId, fieldId);
    }

    @Test
    @DisplayName("POST createReference returns 200 with created reference")
    void createReferenceReturns200() throws Exception {
        BusinessObjectFieldReferenceResponse response = new BusinessObjectFieldReferenceResponse(
                referenceId,
                fieldId,
                targetObjectId,
                displayFieldId,
                false,
                false
        );

        when(referenceService.createReference(eq(businessObjectId), eq(fieldId), any(CreateBusinessObjectFieldReferenceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/reference",
                businessObjectId, fieldId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "targetBusinessObjectId": "%s",
                            "displayFieldId": "%s",
                            "allowMultiple": false,
                            "allowCreate": false
                        }
                        """, targetObjectId, displayFieldId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(referenceId.toString()))
                .andExpect(jsonPath("$.targetBusinessObjectId").value(targetObjectId.toString()))
                .andExpect(jsonPath("$.displayFieldId").value(displayFieldId.toString()));

        verify(referenceService, times(1)).createReference(eq(businessObjectId), eq(fieldId), any());
    }

    @Test
    @DisplayName("PUT updateReference returns 200 with updated reference")
    void updateReferenceReturns200() throws Exception {
        BusinessObjectFieldReferenceResponse response = new BusinessObjectFieldReferenceResponse(
                referenceId,
                fieldId,
                targetObjectId,
                displayFieldId,
                true,
                true
        );

        when(referenceService.updateReference(eq(businessObjectId), eq(fieldId), eq(referenceId), any(UpdateBusinessObjectFieldReferenceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/reference/{referenceId}",
                businessObjectId, fieldId, referenceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "targetBusinessObjectId": "%s",
                            "displayFieldId": "%s",
                            "allowMultiple": true,
                            "allowCreate": true
                        }
                        """, targetObjectId, displayFieldId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(referenceId.toString()))
                .andExpect(jsonPath("$.allowMultiple").value(true))
                .andExpect(jsonPath("$.allowCreate").value(true));

        verify(referenceService, times(1)).updateReference(eq(businessObjectId), eq(fieldId), eq(referenceId), any());
    }

    @Test
    @DisplayName("DELETE reference returns 204 No Content")
    void deleteReferenceReturnsNoContent() throws Exception {
        doNothing().when(referenceService).deleteReference(businessObjectId, fieldId, referenceId);

        mockMvc.perform(delete("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/reference/{referenceId}",
                businessObjectId, fieldId, referenceId))
                .andExpect(status().isNoContent());

        verify(referenceService, times(1)).deleteReference(businessObjectId, fieldId, referenceId);
    }
}
