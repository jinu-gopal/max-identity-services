package com.max.erp.identity.service;

import com.max.erp.identity.entity.Module;
import com.max.erp.identity.entity.ModuleEntitlement;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.repository.ModuleEntitlementRepository;
import com.max.erp.identity.repository.ModuleRepository;
import com.max.erp.identity.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ModuleEntitlementService {

    private final ModuleEntitlementRepository entitlementRepository;
    private final ModuleRepository moduleRepository;
    private final TenantRepository tenantRepository;

    public ModuleEntitlementService(
            ModuleEntitlementRepository entitlementRepository,
            ModuleRepository moduleRepository,
            TenantRepository tenantRepository) {

        this.entitlementRepository = entitlementRepository;
        this.moduleRepository = moduleRepository;
        this.tenantRepository = tenantRepository;
    }

    public ModuleEntitlement enableModule(
            UUID tenantId,
            UUID moduleId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found"
                        ));

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Module not found"
                        ));

        if (!"ACTIVE".equalsIgnoreCase(
                module.getStatus())) {

            throw new IllegalArgumentException(
                    "Only active modules can be enabled"
            );
        }

        ModuleEntitlement entitlement =
                entitlementRepository
                        .findByTenantIdAndModuleId(
                                tenantId,
                                moduleId
                        )
                        .orElse(null);

        Instant now = Instant.now();

        if (entitlement == null) {

            entitlement = new ModuleEntitlement();

            entitlement.setTenant(tenant);
            entitlement.setModule(module);
            entitlement.setStatus("ENABLED");
            entitlement.setEnabledAt(now);
            entitlement.setDisabledAt(null);

        } else {

            if ("ENABLED".equalsIgnoreCase(
                    entitlement.getStatus())) {

                throw new IllegalArgumentException(
                        "Module is already enabled for this tenant"
                );
            }

            entitlement.setStatus("ENABLED");
            entitlement.setEnabledAt(now);
            entitlement.setDisabledAt(null);
        }

        return entitlementRepository.save(
                entitlement
        );
    }

    public ModuleEntitlement disableModule(
            UUID tenantId,
            UUID moduleId) {

        ModuleEntitlement entitlement =
                entitlementRepository
                        .findByTenantIdAndModuleId(
                                tenantId,
                                moduleId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Module entitlement not found"
                                ));

        if ("DISABLED".equalsIgnoreCase(
                entitlement.getStatus())) {

            throw new IllegalArgumentException(
                    "Module is already disabled"
            );
        }

        entitlement.setStatus("DISABLED");
        entitlement.setDisabledAt(Instant.now());

        return entitlementRepository.save(
                entitlement
        );
    }

    /**
     * Authorization-facing module entitlement check.
     *
     * Fail closed:
     * only an explicitly ENABLED entitlement returns true.
     */
    @Transactional(readOnly = true)
    public boolean isModuleEnabled(
            UUID tenantId,
            UUID moduleId) {

        if (tenantId == null || moduleId == null) {
            return false;
        }

        return entitlementRepository
                .existsByTenantIdAndModuleIdAndStatus(
                        tenantId,
                        moduleId,
                        "ENABLED"
                );
    }

    @Transactional(readOnly = true)
    public List<ModuleEntitlement>
    getTenantEntitlements(UUID tenantId) {

        if (!tenantRepository.existsById(tenantId)) {

            throw new IllegalArgumentException(
                    "Tenant not found"
            );
        }

        return entitlementRepository
                .findAllByTenantIdOrderByModuleNameAsc(
                        tenantId
                );
    }

    @Transactional(readOnly = true)
    public List<ModuleEntitlement>
    getEnabledModules(UUID tenantId) {

        if (!tenantRepository.existsById(tenantId)) {

            throw new IllegalArgumentException(
                    "Tenant not found"
            );
        }

        return entitlementRepository
                .findAllByTenantIdAndStatusOrderByModuleNameAsc(
                        tenantId,
                        "ENABLED"
                );
    }
}