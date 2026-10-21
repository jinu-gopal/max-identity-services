package com.max.erp.identity.service;

import com.max.erp.identity.dto.CreateTenantRequest;
import com.max.erp.identity.dto.TenantResponse;
import com.max.erp.identity.dto.UpdateTenantRequest;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class TenantService {

    public static final Set<String> VALID_STATUSES = Set.of(
            "ACTIVE", "SUSPENDED", "ARCHIVED"
    );

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * Create a new tenant.
     */
    public Tenant createTenant(
            String tenantKey,
            String name) {

        if (tenantRepository.existsByTenantKey(tenantKey)) {
            throw new IllegalArgumentException(
                    "Tenant key already exists: " + tenantKey
            );
        }

        Tenant tenant = new Tenant();
        tenant.setTenantKey(tenantKey.trim());
        tenant.setName(name.trim());
        tenant.setStatus("ACTIVE");

        return tenantRepository.save(tenant);
    }

    /**
     * Create tenant via request DTO and return response DTO.
     */
    public TenantResponse createTenantResponse(CreateTenantRequest request) {
        Tenant tenant = createTenant(request.tenantKey(), request.name());
        return TenantResponse.from(tenant);
    }

    /**
     * Get a tenant by ID.
     */
    @Transactional(readOnly = true)
    public Tenant getTenant(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found: " + id
                        ));
    }

    /**
     * Get tenant response by ID.
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantResponse(UUID id) {
        Tenant tenant = getTenant(id);
        return TenantResponse.from(tenant);
    }

    /**
     * Get all tenants.
     */
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    /**
     * Get all tenant responses.
     */
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenantResponses() {
        return getAllTenants().stream()
                .map(TenantResponse::from)
                .toList();
    }

    /**
     * Update tenant.
     */
    public Tenant updateTenant(
            UUID id,
            String name,
            String status) {

        Tenant tenant = getTenant(id);

        if (name != null && !name.isBlank()) {
            tenant.setName(name.trim());
        }

        if (status != null && !status.isBlank()) {
            String upperStatus = status.trim().toUpperCase();
            if (!VALID_STATUSES.contains(upperStatus)) {
                throw new IllegalArgumentException(
                        "Invalid tenant status: " + status + ". Allowed statuses: " + VALID_STATUSES
                );
            }
            tenant.setStatus(upperStatus);
        }

        return tenantRepository.save(tenant);
    }

    /**
     * Update tenant via request DTO and return response DTO.
     */
    public TenantResponse updateTenantResponse(
            UUID id,
            UpdateTenantRequest request) {

        Tenant updated = updateTenant(id, request.name(), request.status());
        return TenantResponse.from(updated);
    }
}