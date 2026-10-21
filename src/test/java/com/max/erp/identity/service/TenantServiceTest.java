package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateTenantRequest;
import com.max.erp.identity.dto.TenantResponse;
import com.max.erp.identity.dto.UpdateTenantRequest;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    private TenantRepository tenantRepository;
    private TenantService tenantService;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantRepository = mock(TenantRepository.class);
        tenantService = new TenantService(tenantRepository);

        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        tenant.setTenantKey("enterprise-demo");
        tenant.setName("Enterprise Demo Tenant");
        tenant.setStatus("ACTIVE");
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        ReflectionTestUtils.setField(tenant, "createdAt", Instant.now());
        ReflectionTestUtils.setField(tenant, "updatedAt", Instant.now());
    }

    @Test
    @DisplayName("createTenant creates tenant successfully")
    void createTenantSuccess() {
        when(tenantRepository.existsByTenantKey("enterprise-demo")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> {
            Tenant t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", tenantId);
            ReflectionTestUtils.setField(t, "createdAt", Instant.now());
            ReflectionTestUtils.setField(t, "updatedAt", Instant.now());
            return t;
        });

        CreateTenantRequest request = new CreateTenantRequest("enterprise-demo", "Enterprise Demo Tenant");
        TenantResponse response = tenantService.createTenantResponse(request);

        assertNotNull(response);
        assertEquals(tenantId, response.id());
        assertEquals("enterprise-demo", response.tenantKey());
        assertEquals("Enterprise Demo Tenant", response.name());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    @DisplayName("createTenant throws when tenantKey already exists")
    void createTenantDuplicateKeyThrows() {
        when(tenantRepository.existsByTenantKey("enterprise-demo")).thenReturn(true);

        CreateTenantRequest request = new CreateTenantRequest("enterprise-demo", "Enterprise Demo Tenant");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tenantService.createTenantResponse(request)
        );

        assertTrue(ex.getMessage().contains("Tenant key already exists"));
    }

    @Test
    @DisplayName("getTenant returns tenant when found")
    void getTenantSuccess() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        TenantResponse response = tenantService.getTenantResponse(tenantId);

        assertNotNull(response);
        assertEquals(tenantId, response.id());
        assertEquals("enterprise-demo", response.tenantKey());
    }

    @Test
    @DisplayName("getTenant throws when tenant not found")
    void getTenantNotFoundThrows() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                tenantService.getTenantResponse(tenantId)
        );
    }

    @Test
    @DisplayName("getAllTenants returns mapped list")
    void getAllTenantsReturnsList() {
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        List<TenantResponse> responses = tenantService.getAllTenantResponses();

        assertEquals(1, responses.size());
        assertEquals("enterprise-demo", responses.get(0).tenantKey());
    }

    @Test
    @DisplayName("updateTenant updates name and status")
    void updateTenantSuccess() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTenantRequest request = new UpdateTenantRequest("Updated Tenant Name", "SUSPENDED");
        TenantResponse response = tenantService.updateTenantResponse(tenantId, request);

        assertEquals("Updated Tenant Name", response.name());
        assertEquals("SUSPENDED", response.status());
    }

    @Test
    @DisplayName("updateTenant throws on invalid status")
    void updateTenantInvalidStatusThrows() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        UpdateTenantRequest request = new UpdateTenantRequest("Updated Tenant Name", "DELETED");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tenantService.updateTenantResponse(tenantId, request)
        );

        assertTrue(ex.getMessage().contains("Invalid tenant status"));
    }
}
