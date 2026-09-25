package com.max.erp.identity.repository;

import com.max.erp.identity.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuleRepository
        extends JpaRepository<Module, UUID> {

    Optional<Module> findByModuleKey(String moduleKey);

    boolean existsByModuleKey(String moduleKey);

    List<Module> findAllByStatusOrderByNameAsc(String status);
}