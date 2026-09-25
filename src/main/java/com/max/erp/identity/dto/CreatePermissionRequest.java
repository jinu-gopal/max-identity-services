package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(

        @NotBlank(message = "Permission key is required")
        @Size(max = 150, message = "Permission key must not exceed 150 characters")
        @Pattern(
                regexp = "^[A-Z][A-Z0-9_]*(\\.[A-Z][A-Z0-9_]*)?$",
                message = "Permission key must use uppercase module.action format"
        )
        String permissionKey,

        @NotBlank(message = "Permission name is required")
        @Size(max = 200, message = "Permission name must not exceed 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Module key is required")
        @Size(max = 100, message = "Module key must not exceed 100 characters")
        @Pattern(
                regexp = "^[A-Z][A-Z0-9_]*$",
                message = "Module key must contain uppercase letters, numbers and underscores"
        )
        String moduleKey,

        @NotBlank(message = "Action key is required")
        @Size(max = 100, message = "Action key must not exceed 100 characters")
        @Pattern(
                regexp = "^[A-Z][A-Z0-9_]*$",
                message = "Action key must contain uppercase letters, numbers and underscores"
        )
        String actionKey
) {
}