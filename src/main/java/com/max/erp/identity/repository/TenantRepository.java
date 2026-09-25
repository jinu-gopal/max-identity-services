package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByTenantKey(String tenantKey);

    boolean existsByTenantKey(String tenantKey);
}
