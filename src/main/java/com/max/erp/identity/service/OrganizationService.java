package com.max.erp.identity.service;

import com.max.erp.identity.entity.Organization;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.OrganizationRepository;
import com.max.erp.identity.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

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
                                "Tenant not found"
                        ));

        if (organizationRepository
                .existsByTenantIdAndOrganizationKey(
                        tenantId,
                        organizationKey)) {

            throw new IllegalArgumentException(
                    "Organization key already exists"
            );
        }

        Organization organization = new Organization();

        organization.setTenant(tenant);
        organization.setOrganizationKey(organizationKey);
        organization.setName(name);

        if (organizationType != null &&
                !organizationType.isBlank()) {

            organization.setOrganizationType(
                    organizationType
            );
        }

        if (parentOrganizationId != null) {

            Organization parent =
                    organizationRepository
                            .findById(parentOrganizationId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Parent organization not found"
                                    ));

            if (!parent.getTenant()
                    .getId()
                    .equals(tenantId)) {

                throw new IllegalArgumentException(
                        "Parent organization belongs to another tenant"
                );
            }

            organization.setParentOrganization(parent);
        }

        organization.setStatus("ACTIVE");

        return organizationRepository.save(organization);
    }

    /**
     * Get organization by ID.
     */
    @Transactional(readOnly = true)
    public Organization getOrganization(UUID id) {

        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organization not found"
                        ));
    }

    /**
     * Get all organizations for a tenant.
     */
    @Transactional(readOnly = true)
    public List<Organization> getOrganizations(
            UUID tenantId) {

        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException(
                    "Tenant not found"
            );
        }

        return organizationRepository
                .findAllByTenantIdOrderByNameAsc(tenantId);
    }

    /**
     * Update organization.
     */
    public Organization updateOrganization(
            UUID id,
            String name,
            String status) {

        Organization organization =
                getOrganization(id);

        if (name != null && !name.isBlank()) {
            organization.setName(name);
        }

        if (status != null && !status.isBlank()) {
            organization.setStatus(status);
        }

        return organizationRepository.save(
                organization
        );
    }
}