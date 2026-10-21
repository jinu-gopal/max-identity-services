package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateOrganizationRequest;
import com.max.erp.identity.dto.OrganizationResponse;
import com.max.erp.identity.dto.UpdateOrganizationRequest;
import com.max.erp.identity.entity.Organization;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.OrganizationRepository;
import com.max.erp.identity.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    public static final Set<String> VALID_TYPES = Set.of(
            "COMPANY", "DIVISION", "BRANCH", "DEPARTMENT", "UNIT", "LOCATION"
    );

    public static final Set<String> VALID_STATUSES = Set.of(
            "ACTIVE", "INACTIVE", "ARCHIVED"
    );

    private final OrganizationRepository organizationRepository;
    private final TenantRepository tenantRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            TenantRepository tenantRepository) {

        this.organizationRepository = organizationRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Create an organization under a tenant.
     */
    public Organization createOrganization(
            UUID tenantId,
            String organizationKey,
            String name,
            String organizationType,
            UUID parentOrganizationId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found: " + tenantId
                        ));

        if (organizationRepository.existsByTenantIdAndOrganizationKey(
                tenantId, organizationKey)) {
            throw new IllegalArgumentException(
                    "Organization key already exists: " + organizationKey
            );
        }

        String typeToSet = "UNIT";
        if (organizationType != null && !organizationType.isBlank()) {
            String upperType = organizationType.trim().toUpperCase();
            if (!VALID_TYPES.contains(upperType)) {
                throw new IllegalArgumentException(
                        "Invalid organization type: " + organizationType +
                        ". Allowed types: " + VALID_TYPES
                );
            }
            typeToSet = upperType;
        }

        Organization organization = new Organization();
        organization.setTenant(tenant);
        organization.setOrganizationKey(organizationKey.trim());
        organization.setName(name.trim());
        organization.setOrganizationType(typeToSet);
        organization.setStatus("ACTIVE");

        if (parentOrganizationId != null) {
            Organization parent = organizationRepository.findById(parentOrganizationId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Parent organization not found: " + parentOrganizationId
                            ));

            if (!parent.getTenant().getId().equals(tenantId)) {
                throw new IllegalArgumentException(
                        "Parent organization belongs to another tenant"
                );
            }

            organization.setParentOrganization(parent);
        }

        return organizationRepository.save(organization);
    }

    /**
     * Create organization via request DTO and return response DTO.
     */
    public OrganizationResponse createOrganizationResponse(
            UUID tenantId,
            CreateOrganizationRequest request) {

        Organization org = createOrganization(
                tenantId,
                request.organizationKey(),
                request.name(),
                request.organizationType(),
                request.parentOrganizationId()
        );

        return OrganizationResponse.from(org);
    }

    /**
     * Get organization by ID.
     */
    @Transactional(readOnly = true)
    public Organization getOrganization(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organization not found: " + id
                        ));
    }

    /**
     * Get organization by ID scoped to tenant.
     */
    @Transactional(readOnly = true)
    public Organization getOrganization(UUID tenantId, UUID id) {
        return organizationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organization not found for tenant: " + id
                        ));
    }

    /**
     * Get organization response by ID scoped to tenant.
     */
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationResponse(UUID tenantId, UUID id) {
        Organization org = getOrganization(tenantId, id);
        return OrganizationResponse.from(org);
    }

    /**
     * Get all organizations for a tenant.
     */
    @Transactional(readOnly = true)
    public List<Organization> getOrganizations(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException(
                    "Tenant not found: " + tenantId
            );
        }

        return organizationRepository.findAllByTenantIdOrderByNameAsc(tenantId);
    }

    /**
     * Get all organization responses for a tenant.
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getOrganizationResponses(UUID tenantId) {
        return getOrganizations(tenantId).stream()
                .map(OrganizationResponse::from)
                .toList();
    }

    /**
     * Update organization basic fields.
     */
    public Organization updateOrganization(
            UUID id,
            String name,
            String status) {

        Organization organization = getOrganization(id);

        if (name != null && !name.isBlank()) {
            organization.setName(name.trim());
        }

        if (status != null && !status.isBlank()) {
            String upperStatus = status.trim().toUpperCase();
            if (!VALID_STATUSES.contains(upperStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status: " + status + ". Allowed statuses: " + VALID_STATUSES
                );
            }
            organization.setStatus(upperStatus);
        }

        return organizationRepository.save(organization);
    }

    /**
     * Update organization with full DTO scoped to tenant.
     */
    public OrganizationResponse updateOrganization(
            UUID tenantId,
            UUID id,
            UpdateOrganizationRequest request) {

        Organization organization = getOrganization(tenantId, id);

        if (request.name() != null && !request.name().isBlank()) {
            organization.setName(request.name().trim());
        }

        if (request.status() != null && !request.status().isBlank()) {
            String upperStatus = request.status().trim().toUpperCase();
            if (!VALID_STATUSES.contains(upperStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status: " + request.status() + ". Allowed statuses: " + VALID_STATUSES
                );
            }
            organization.setStatus(upperStatus);
        }

        if (request.organizationType() != null && !request.organizationType().isBlank()) {
            String upperType = request.organizationType().trim().toUpperCase();
            if (!VALID_TYPES.contains(upperType)) {
                throw new IllegalArgumentException(
                        "Invalid organization type: " + request.organizationType() +
                        ". Allowed types: " + VALID_TYPES
                );
            }
            organization.setOrganizationType(upperType);
        }

        // Parent hierarchy update if specified
        if (request.parentOrganizationId() != null) {
            UUID newParentId = request.parentOrganizationId();
            if (newParentId.equals(id)) {
                throw new IllegalArgumentException(
                        "An organization cannot be its own parent"
                );
            }

            Organization parent = organizationRepository.findById(newParentId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Parent organization not found: " + newParentId
                            ));

            if (!parent.getTenant().getId().equals(tenantId)) {
                throw new IllegalArgumentException(
                        "Parent organization belongs to another tenant"
                );
            }

            // Cycle detection: traverse parent chain up to ensure 'id' is not an ancestor
            Set<UUID> visited = new HashSet<>();
            visited.add(id);
            Organization ancestor = parent;
            while (ancestor != null) {
                if (visited.contains(ancestor.getId())) {
                    throw new IllegalArgumentException(
                            "Circular reference detected: cannot assign parent that descends from this organization"
                    );
                }
                visited.add(ancestor.getId());
                ancestor = ancestor.getParentOrganization();
            }

            organization.setParentOrganization(parent);
        }

        Organization saved = organizationRepository.save(organization);
        return OrganizationResponse.from(saved);
    }
}