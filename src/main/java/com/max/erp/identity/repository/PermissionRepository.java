package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository
        extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByPermissionKey(
            String permissionKey
    );

    List<Permission> findAllByModuleKeyOrderByNameAsc(
            String moduleKey
    );

    List<Permission> findAllByOrderByModuleKeyAscNameAsc();

    boolean existsByPermissionKey(String permissionKey);
}
