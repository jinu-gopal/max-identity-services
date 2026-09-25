package com.max.erp.identity.controller;

import com.max.erp.identity.dto.CreatePermissionRequest;
import com.max.erp.identity.dto.UpdatePermissionRequest;
import com.max.erp.identity.entity.Permission;
import com.max.erp.identity.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(
            PermissionService permissionService) {

        this.permissionService = permissionService;
    }

    /**
     * Get all permissions.
     */
    @GetMapping
    public ResponseEntity<List<Permission>>
    getAllPermissions() {

        return ResponseEntity.ok(
                permissionService.getAllPermissions()
        );
    }

    /**
     * Get permission by ID.
     */
    @GetMapping("/{permissionId}")
    public ResponseEntity<Permission>
    getPermission(
            @PathVariable UUID permissionId) {

        return ResponseEntity.ok(
                permissionService
                        .getPermission(permissionId)
        );
    }

    /**
     * Get permissions for a module.
     */
    @GetMapping("/module/{moduleKey}")
    public ResponseEntity<List<Permission>>
    getModulePermissions(
            @PathVariable String moduleKey) {

        return ResponseEntity.ok(
                permissionService
                        .getModulePermissions(moduleKey)
        );
    }

    /**
     * Create permission.
     */
    @PostMapping
    public ResponseEntity<Permission>
    createPermission(
            @Valid @RequestBody
            CreatePermissionRequest request) {

        Permission permission =
                permissionService
                        .createPermission(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permission);
    }

    /**
     * Update permission metadata.
     */
    @PutMapping("/{permissionId}")
    public ResponseEntity<Permission>
    updatePermission(
            @PathVariable UUID permissionId,
            @Valid @RequestBody
            UpdatePermissionRequest request) {

        return ResponseEntity.ok(
                permissionService.updatePermission(
                        permissionId,
                        request
                )
        );
    }
}