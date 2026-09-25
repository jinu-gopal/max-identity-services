package com.max.erp.identity.service;

import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TenantService {

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
                    "Tenant key already exists"
            );
        }

        Tenant tenant = new Tenant();

        tenant.setTenantKey(tenantKey);
        tenant.setName(name);
        tenant.setStatus("ACTIVE");

        return tenantRepository.save(tenant);
    }

    /**
     * Get a tenant by ID.
     */
    @Transactional(readOnly = true)
    public Tenant getTenant(UUID id) {

        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found"
                        ));
    }

    /**
     * Get all tenants.
     */
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {

        return tenantRepository.findAll();
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
            tenant.setName(name);
        }

        if (status != null && !status.isBlank()) {
            tenant.setStatus(status);
        }

        return tenantRepository.save(tenant);
    }
}