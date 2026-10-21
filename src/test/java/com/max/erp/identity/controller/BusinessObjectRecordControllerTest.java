package com.max.erp.identity.controller;

import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.BusinessObjectRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessObjectRecordControllerTest {

    private BusinessObjectRecordService recordService;
    private MockMvc mockMvc;

    private UUID businessObjectId;
    private UUID recordId;

    @BeforeEach
    void setUp() {
        recordService = mock(BusinessObjectRecordService.class);
        BusinessObjectRecordController controller = new BusinessObjectRecordController(recordService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        businessObjectId = UUID.randomUUID();
        recordId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET records returns 200 with list of records")
    void getRecordsReturns200() throws Exception {
        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setBusinessObjectId(businessObjectId);
        record.setRecordData(Map.of("name", "Acme Corp"));
        record.setCreatedAt(OffsetDateTime.now());

        when(recordService.getRecords(businessObjectId)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/records", businessObjectId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(recordId.toString()))
                .andExpect(jsonPath("$[0].recordData.name").value("Acme Corp"));

        verify(recordService, times(1)).getRecords(businessObjectId);
    }

    @Test
    @DisplayName("GET record returns 200 with single record")
    void getRecordReturns200() throws Exception {
        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setBusinessObjectId(businessObjectId);
        record.setRecordData(Map.of("name", "Acme Corp"));

        when(recordService.getRecord(businessObjectId, recordId)).thenReturn(record);

        mockMvc.perform(get("/api/v1/configuration/business-objects/{businessObjectId}/records/{recordId}",
                businessObjectId, recordId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recordId.toString()))
                .andExpect(jsonPath("$.recordData.name").value("Acme Corp"));

        verify(recordService, times(1)).getRecord(businessObjectId, recordId);
    }

    @Test
    @DisplayName("POST createRecord returns 200 with saved record")
    void createRecordReturns200() throws Exception {
        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setBusinessObjectId(businessObjectId);
        record.setRecordData(Map.of("company_name", "Acme Corp"));

        when(recordService.createRecord(eq(businessObjectId), any(), any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/configuration/business-objects/{businessObjectId}/records", businessObjectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "company_name": "Acme Corp"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recordId.toString()))
                .andExpect(jsonPath("$.recordData.company_name").value("Acme Corp"));

        verify(recordService, times(1)).createRecord(eq(businessObjectId), any(), any());
    }

    @Test
    @DisplayName("PUT updateRecord returns 200 with updated record")
    void updateRecordReturns200() throws Exception {
        BusinessObjectRecord record = new BusinessObjectRecord();
        record.setId(recordId);
        record.setBusinessObjectId(businessObjectId);
        record.setRecordData(Map.of("company_name", "Acme Updated"));

        when(recordService.updateRecord(eq(businessObjectId), eq(recordId), any())).thenReturn(record);

        mockMvc.perform(put("/api/v1/configuration/business-objects/{businessObjectId}/records/{recordId}",
                businessObjectId, recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "company_name": "Acme Updated"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordData.company_name").value("Acme Updated"));

        verify(recordService, times(1)).updateRecord(eq(businessObjectId), eq(recordId), any());
    }

    @Test
    @DisplayName("DELETE deleteRecord returns 204 No Content")
    void deleteRecordReturnsNoContent() throws Exception {
        doNothing().when(recordService).deleteRecord(businessObjectId, recordId);

        mockMvc.perform(delete("/api/v1/configuration/business-objects/{businessObjectId}/records/{recordId}",
                businessObjectId, recordId))
                .andExpect(status().isNoContent());

        verify(recordService, times(1)).deleteRecord(businessObjectId, recordId);
    }
}
