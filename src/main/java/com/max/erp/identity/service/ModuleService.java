package com.max.erp.identity.service;

import com.max.erp.identity.entity.Module;
import com.max.erp.identity.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Transactional(readOnly = true)
    public Module getModule(UUID id) {

        return moduleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Module not found"
                        ));
    }

    @Transactional(readOnly = true)
    public Module getByModuleKey(String moduleKey) {

        return moduleRepository.findByModuleKey(moduleKey)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Module not found"
                        ));
    }

    @Transactional(readOnly = true)
    public List<Module> getActiveModules() {

        return moduleRepository
                .findAllByStatusOrderByNameAsc("ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<Module> getAllModules() {

        return moduleRepository.findAll();
    }

    /**
     * Creates a platform module.
     *
     * Module creation is an internal platform operation.
     * Customer administrators do not create arbitrary
     * platform modules.
     */
    public Module createModule(
            String moduleKey,
            String name,
            String description) {

        if (moduleRepository.existsByModuleKey(moduleKey)) {

            throw new IllegalArgumentException(
                    "Module key already exists"
            );
        }

        Module module = new Module();

        module.setModuleKey(moduleKey);
        module.setName(name);
        module.setDescription(description);
        module.setVersionNumber(1);
        module.setStatus("ACTIVE");

        return moduleRepository.save(module);
    }
}