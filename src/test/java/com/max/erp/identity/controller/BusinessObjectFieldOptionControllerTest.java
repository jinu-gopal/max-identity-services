package com.max.erp.identity.controller;

import com.max.erp.identity.dto.BusinessObjectFieldOptionResponse;
import com.max.erp.identity.dto.CreateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.dto.UpdateBusinessObjectFieldOptionRequest;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.BusinessObjectFieldOptionService;
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

class BusinessObjectFieldOptionControllerTest {

    private BusinessObjectFieldOptionService optionService;
    private MockMvc mockMvc;

    private UUID businessObjectId;
    private UUID fieldId;
    private UUID optionId;

    @BeforeEach
    void setUp() {
        optionService = mock(BusinessObjectFieldOptionService.class);
        BusinessObjectFieldOptionController controller = new BusinessObjectFieldOptionController(optionService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        businessObjectId = UUID.randomUUID();
        fieldId = UUID.randomUUID();
        optionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET options returns 200 with option array")
    void getOptionsReturns200() throws Exception {
        BusinessObjectFieldOptionResponse opt1 = new BusinessObjectFieldOptionResponse(
                UUID.randomUUID(),
                fieldId,
                "Corporate",
                "Corporate Account",
                0,
                true,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(optionService.getOptions(businessObjectId, fieldId)).thenReturn(List.of(opt1));

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/options",
                businessObjectId, fieldId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].optionValue").value("Corporate"))
                .andExpect(jsonPath("$[0].optionLabel").value("Corporate Account"));

        verify(optionService, times(1)).getOptions(businessObjectId, fieldId);
    }

    @Test
    @DisplayName("POST createOption returns 200 with created option")
    void createOptionReturns200() throws Exception {
        BusinessObjectFieldOptionResponse response = new BusinessObjectFieldOptionResponse(
                optionId,
                fieldId,
                "Business",
                "Business Customer",
                1,
                true,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(optionService.createOption(eq(businessObjectId), eq(fieldId), any(CreateBusinessObjectFieldOptionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/options",
                businessObjectId, fieldId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "optionValue": "Business",
                            "optionLabel": "Business Customer",
                            "displayOrder": 1,
                            "active": true,
                            "defaultOption": false
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(optionId.toString()))
                .andExpect(jsonPath("$.optionValue").value("Business"))
                .andExpect(jsonPath("$.active").value(true));

        verify(optionService, times(1)).createOption(eq(businessObjectId), eq(fieldId), any());
    }

    @Test
    @DisplayName("PUT updateOption returns 200 with updated option")
    void updateOptionReturns200() throws Exception {
        BusinessObjectFieldOptionResponse response = new BusinessObjectFieldOptionResponse(
                optionId,
                fieldId,
                "Corporate",
                "Updated Label",
                0,
                false,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(optionService.updateOption(eq(businessObjectId), eq(fieldId), eq(optionId), any(UpdateBusinessObjectFieldOptionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/options/{optionId}",
                businessObjectId, fieldId, optionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "optionLabel": "Updated Label",
                            "active": false
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionLabel").value("Updated Label"))
                .andExpect(jsonPath("$.active").value(false));

        verify(optionService, times(1)).updateOption(eq(businessObjectId), eq(fieldId), eq(optionId), any());
    }

    @Test
    @DisplayName("DELETE option returns 204 No Content")
    void deleteOptionReturnsNoContent() throws Exception {
        doNothing().when(optionService).deleteOption(businessObjectId, fieldId, optionId);

        mockMvc.perform(delete("/api/v1/configuration/business-objects/{businessObjectId}/fields/{fieldId}/options/{optionId}",
                businessObjectId, fieldId, optionId))
                .andExpect(status().isNoContent());

        verify(optionService, times(1)).deleteOption(businessObjectId, fieldId, optionId);
    }
}
