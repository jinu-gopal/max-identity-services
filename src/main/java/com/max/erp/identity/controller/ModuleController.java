package com.max.erp.identity.controller;

import com.max.erp.identity.entity.Module;
import com.max.erp.identity.service.ModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public ResponseEntity<List<Module>> getAllModules() {

        return ResponseEntity.ok(
                moduleService.getAllModules()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<Module>> getActiveModules() {

        return ResponseEntity.ok(
                moduleService.getActiveModules()
        );
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<Module> getModule(
            @PathVariable UUID moduleId) {

        return ResponseEntity.ok(
                moduleService.getModule(moduleId)
        );
    }

    @GetMapping("/key/{moduleKey}")
    public ResponseEntity<Module> getByModuleKey(
            @PathVariable String moduleKey) {

        return ResponseEntity.ok(
                moduleService.getByModuleKey(moduleKey)
        );
    }

    /**
     * Platform module registration.
     *
     * This endpoint will be protected by platform-level
     * authorization when the security administration
     * permission layer is enabled.
     */
    @PostMapping
    public ResponseEntity<Module> createModule(
            @RequestParam String moduleKey,
            @RequestParam String name,
            @RequestParam(required = false)
            String description) {

        Module module = moduleService.createModule(
                moduleKey,
                name,
                description
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(module);
    }
}