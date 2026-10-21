package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateOrganizationRequest;
import com.max.erp.identity.dto.OrganizationResponse;
import com.max.erp.identity.dto.UpdateOrganizationRequest;
import com.max.erp.identity.entity.Organization;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.OrganizationRepository;
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

class OrganizationServiceTest {

    private OrganizationRepository organizationRepository;
    private TenantRepository tenantRepository;
    private OrganizationService organizationService;

    private UUID tenantId;
    private Tenant tenant;
    private UUID orgId;
    private Organization rootOrg;

    @BeforeEach
    void setUp() {
        organizationRepository = mock(OrganizationRepository.class);
        tenantRepository = mock(TenantRepository.class);
        organizationService = new OrganizationService(organizationRepository, tenantRepository);

        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        tenant.setTenantKey("test-tenant");
        tenant.setName("Test Tenant");
        tenant.setStatus("ACTIVE");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        orgId = UUID.randomUUID();
        rootOrg = new Organization();
        rootOrg.setTenant(tenant);
        rootOrg.setOrganizationKey("corp-hq");
        rootOrg.setName("Max Global HQ");
        rootOrg.setOrganizationType("COMPANY");
        rootOrg.setStatus("ACTIVE");
        ReflectionTestUtils.setField(rootOrg, "id", orgId);
        ReflectionTestUtils.setField(rootOrg, "createdAt", Instant.now());
        ReflectionTestUtils.setField(rootOrg, "updatedAt", Instant.now());
    }

    @Test
    @DisplayName("createOrganization creates top-level root organization successfully")
    void createOrganizationRootSuccess() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(organizationRepository.existsByTenantIdAndOrganizationKey(tenantId, "corp-hq")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization o = inv.getArgument(0);
            ReflectionTestUtils.setField(o, "id", orgId);
            ReflectionTestUtils.setField(o, "createdAt", Instant.now());
            ReflectionTestUtils.setField(o, "updatedAt", Instant.now());
            return o;
        });

        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "corp-hq", "Max Global HQ", "COMPANY", null
        );

        OrganizationResponse response = organizationService.createOrganizationResponse(tenantId, request);

        assertNotNull(response);
        assertEquals(orgId, response.id());
        assertEquals("corp-hq", response.organizationKey());
        assertEquals("Max Global HQ", response.name());
        assertEquals("COMPANY", response.organizationType());
        assertNull(response.parentOrganizationId());
    }

    @Test
    @DisplayName("createOrganization creates child node linked to parent")
    void createOrganizationChildSuccess() {
        UUID childId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(organizationRepository.existsByTenantIdAndOrganizationKey(tenantId, "finance-dept")).thenReturn(false);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(rootOrg));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization o = inv.getArgument(0);
            ReflectionTestUtils.setField(o, "id", childId);
            ReflectionTestUtils.setField(o, "createdAt", Instant.now());
            ReflectionTestUtils.setField(o, "updatedAt", Instant.now());
            return o;
        });

        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "finance-dept", "Finance Department", "DEPARTMENT", orgId
        );

        OrganizationResponse response = organizationService.createOrganizationResponse(tenantId, request);

        assertNotNull(response);
        assertEquals(childId, response.id());
        assertEquals("finance-dept", response.organizationKey());
        assertEquals(orgId, response.parentOrganizationId());
        assertEquals("Max Global HQ", response.parentOrganizationName());
    }

    @Test
    @DisplayName("createOrganization throws if tenant not found")
    void createOrganizationTenantNotFound() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "corp-hq", "Max Global HQ", "COMPANY", null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createOrganizationResponse(tenantId, request)
        );
        assertTrue(ex.getMessage().contains("Tenant not found"));
    }

    @Test
    @DisplayName("createOrganization throws if organizationKey already exists")
    void createOrganizationDuplicateKey() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(organizationRepository.existsByTenantIdAndOrganizationKey(tenantId, "corp-hq")).thenReturn(true);

        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "corp-hq", "Max Global HQ", "COMPANY", null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createOrganizationResponse(tenantId, request)
        );
        assertTrue(ex.getMessage().contains("Organization key already exists"));
    }

    @Test
    @DisplayName("createOrganization throws on invalid organization type")
    void createOrganizationInvalidType() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(organizationRepository.existsByTenantIdAndOrganizationKey(tenantId, "corp-hq")).thenReturn(false);

        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "corp-hq", "Max Global HQ", "INVALID_TYPE", null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createOrganizationResponse(tenantId, request)
        );
        assertTrue(ex.getMessage().contains("Invalid organization type"));
    }

    @Test
    @DisplayName("createOrganization throws if parent belongs to another tenant")
    void createOrganizationParentDifferentTenant() {
        UUID otherTenantId = UUID.randomUUID();
        Tenant otherTenant = new Tenant();
        ReflectionTestUtils.setField(otherTenant, "id", otherTenantId);

        Organization foreignParent = new Organization();
        foreignParent.setTenant(otherTenant);
        foreignParent.setName("Foreign Org");
        UUID foreignParentId = UUID.randomUUID();
        ReflectionTestUtils.setField(foreignParent, "id", foreignParentId);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(organizationRepository.existsByTenantIdAndOrganizationKey(tenantId, "sub-org")).thenReturn(false);
        when(organizationRepository.findById(foreignParentId)).thenReturn(Optional.of(foreignParent));

        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "sub-org", "Sub Org", "UNIT", foreignParentId
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createOrganizationResponse(tenantId, request)
        );
        assertTrue(ex.getMessage().contains("Parent organization belongs to another tenant"));
    }

    @Test
    @DisplayName("getOrganizationResponses returns list of mapped DTOs for tenant")
    void getOrganizationResponsesSuccess() {
        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(organizationRepository.findAllByTenantIdOrderByNameAsc(tenantId)).thenReturn(List.of(rootOrg));

        List<OrganizationResponse> responses = organizationService.getOrganizationResponses(tenantId);

        assertEquals(1, responses.size());
        assertEquals("corp-hq", responses.get(0).organizationKey());
        assertEquals("Max Global HQ", responses.get(0).name());
    }

    @Test
    @DisplayName("updateOrganization updates name, status and type")
    void updateOrganizationSuccess() {
        when(organizationRepository.findByIdAndTenantId(orgId, tenantId)).thenReturn(Optional.of(rootOrg));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Max International HQ", "INACTIVE", "DIVISION", null
        );

        OrganizationResponse updated = organizationService.updateOrganization(tenantId, orgId, request);

        assertEquals("Max International HQ", updated.name());
        assertEquals("INACTIVE", updated.status());
        assertEquals("DIVISION", updated.organizationType());
    }

    @Test
    @DisplayName("updateOrganization throws if organization is set as its own parent")
    void updateOrganizationSelfParentThrows() {
        when(organizationRepository.findByIdAndTenantId(orgId, tenantId)).thenReturn(Optional.of(rootOrg));

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Max Global HQ", "ACTIVE", "COMPANY", orgId
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                organizationService.updateOrganization(tenantId, orgId, request)
        );
        assertTrue(ex.getMessage().contains("cannot be its own parent"));
    }

    @Test
    @DisplayName("updateOrganization throws when circular hierarchy cycle is detected")
    void updateOrganizationCycleThrows() {
        // rootOrg -> childOrg. Trying to set rootOrg's parent to childOrg should fail!
        UUID childId = UUID.randomUUID();
        Organization childOrg = new Organization();
        childOrg.setTenant(tenant);
        childOrg.setName("Operations Division");
        childOrg.setParentOrganization(rootOrg);
        ReflectionTestUtils.setField(childOrg, "id", childId);

        when(organizationRepository.findByIdAndTenantId(orgId, tenantId)).thenReturn(Optional.of(rootOrg));
        when(organizationRepository.findById(childId)).thenReturn(Optional.of(childOrg));

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Max Global HQ", "ACTIVE", "COMPANY", childId
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                organizationService.updateOrganization(tenantId, orgId, request)
        );
        assertTrue(ex.getMessage().contains("Circular reference detected"));
    }
}
