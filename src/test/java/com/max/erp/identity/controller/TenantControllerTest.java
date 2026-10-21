package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateTenantRequest;
import com.max.erp.identity.dto.TenantResponse;
import com.max.erp.identity.dto.UpdateTenantRequest;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TenantControllerTest {

    private TenantService tenantService;
    private MockMvc mockMvc;

    private UUID tenantId;
    private TenantResponse sampleTenantResponse;

    @BeforeEach
    void setUp() {
        tenantService = mock(TenantService.class);
        TenantController controller = new TenantController(tenantService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        tenantId = UUID.randomUUID();

        sampleTenantResponse = new TenantResponse(
                tenantId,
                "security-test",
                "MAX Security Test Tenant",
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/tenants returns 200 with tenant list")
    void getAllTenantsReturns200() throws Exception {
        when(tenantService.getAllTenantResponses()).thenReturn(List.of(sampleTenantResponse));

        mockMvc.perform(get("/api/v1/tenants")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tenantId.toString()))
                .andExpect(jsonPath("$[0].tenantKey").value("security-test"))
                .andExpect(jsonPath("$[0].name").value("MAX Security Test Tenant"));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId} returns 200 with tenant detail")
    void getTenantReturns200() throws Exception {
        when(tenantService.getTenantResponse(tenantId)).thenReturn(sampleTenantResponse);

        mockMvc.perform(get("/api/v1/tenants/{tenantId}", tenantId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenantId.toString()))
                .andExpect(jsonPath("$.tenantKey").value("security-test"))
                .andExpect(jsonPath("$.name").value("MAX Security Test Tenant"));
    }

    @Test
    @DisplayName("POST /api/v1/tenants with JSON body creates tenant and returns 201")
    void createTenantJsonReturns201() throws Exception {
        TenantResponse createdResponse = new TenantResponse(
                UUID.randomUUID(),
                "new-tenant",
                "New Enterprise Tenant",
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );

        when(tenantService.createTenantResponse(any(CreateTenantRequest.class))).thenReturn(createdResponse);

        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "tenantKey": "new-tenant",
                            "name": "New Enterprise Tenant"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantKey").value("new-tenant"))
                .andExpect(jsonPath("$.name").value("New Enterprise Tenant"));
    }

    @Test
    @DisplayName("PUT /api/v1/tenants/{tenantId} with JSON body updates tenant and returns 200")
    void updateTenantJsonReturns200() throws Exception {
        TenantResponse updatedResponse = new TenantResponse(
                tenantId,
                "security-test",
                "Updated Tenant",
                "SUSPENDED",
                Instant.now(),
                Instant.now()
        );

        when(tenantService.updateTenantResponse(eq(tenantId), any(UpdateTenantRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/tenants/{tenantId}", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Updated Tenant",
                            "status": "SUSPENDED"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Tenant"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }
}
