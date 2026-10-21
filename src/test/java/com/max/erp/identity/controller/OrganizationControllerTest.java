package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreateOrganizationRequest;
import com.max.erp.identity.dto.OrganizationResponse;
import com.max.erp.identity.dto.UpdateOrganizationRequest;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.OrganizationService;
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

class OrganizationControllerTest {

    private OrganizationService organizationService;
    private MockMvc mockMvc;

    private UUID tenantId;
    private UUID orgId;
    private OrganizationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        organizationService = mock(OrganizationService.class);
        OrganizationController controller = new OrganizationController(organizationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        tenantId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        sampleResponse = new OrganizationResponse(
                orgId,
                tenantId,
                "test-tenant",
                null,
                null,
                "corp-hq",
                "Max Global HQ",
                "COMPANY",
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId}/organizations returns 200 with list")
    void getOrganizationsReturns200() throws Exception {
        when(organizationService.getOrganizationResponses(tenantId)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/organizations", tenantId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orgId.toString()))
                .andExpect(jsonPath("$[0].organizationKey").value("corp-hq"))
                .andExpect(jsonPath("$[0].name").value("Max Global HQ"))
                .andExpect(jsonPath("$[0].organizationType").value("COMPANY"));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId}/organizations/{orgId} returns 200 with item")
    void getOrganizationReturns200() throws Exception {
        when(organizationService.getOrganizationResponse(tenantId, orgId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/organizations/{orgId}", tenantId, orgId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.organizationKey").value("corp-hq"))
                .andExpect(jsonPath("$.name").value("Max Global HQ"));
    }

    @Test
    @DisplayName("POST /api/v1/tenants/{tenantId}/organizations creates organization and returns 201")
    void createOrganizationReturns201() throws Exception {
        when(organizationService.createOrganizationResponse(eq(tenantId), any(CreateOrganizationRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/organizations", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "organizationKey": "corp-hq",
                            "name": "Max Global HQ",
                            "organizationType": "COMPANY"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.organizationKey").value("corp-hq"));
    }

    @Test
    @DisplayName("POST /api/v1/tenants/{tenantId}/organizations with invalid payload returns 400")
    void createOrganizationInvalidPayloadReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/organizations", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "organizationKey": "",
                            "name": "",
                            "organizationType": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/tenants/{tenantId}/organizations/{orgId} updates organization and returns 200")
    void updateOrganizationReturns200() throws Exception {
        OrganizationResponse updatedResponse = new OrganizationResponse(
                orgId,
                tenantId,
                "test-tenant",
                null,
                null,
                "corp-hq",
                "Updated Org Name",
                "DIVISION",
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );

        when(organizationService.updateOrganization(eq(tenantId), eq(orgId), any(UpdateOrganizationRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/tenants/{tenantId}/organizations/{orgId}", tenantId, orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Updated Org Name",
                            "status": "ACTIVE",
                            "organizationType": "DIVISION"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Org Name"))
                .andExpect(jsonPath("$.organizationType").value("DIVISION"));
    }
}
