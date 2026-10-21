package com.max.erp.identity.controller;

import com.max.erp.identity.dto.AssignPermissionRequest;
import com.max.erp.identity.dto.RolePermissionResponse;
import com.max.erp.identity.entity.RolePermission;
import com.max.erp.identity.service.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/{roleId}/permissions")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(
            RolePermissionService rolePermissionService) {

        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping
    public ResponseEntity<List<RolePermissionResponse>>
    getPermissions(
            @PathVariable UUID roleId) {

        List<RolePermissionResponse> response = rolePermissionService
                .getPermissions(roleId)
                .stream()
                .map(RolePermissionResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<RolePermissionResponse>
    assignPermission(
            @PathVariable UUID roleId,
            @Valid @RequestBody
            AssignPermissionRequest request) {

        RolePermission rolePermission = rolePermissionService
                .assignPermission(
                        roleId,
                        request.permissionId()
                );

        return ResponseEntity.ok(
                RolePermissionResponse.from(rolePermission)
        );
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void>
    removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        rolePermissionService.removePermission(
                roleId,
                permissionId
        );

        return ResponseEntity.noContent().build();
    }
}