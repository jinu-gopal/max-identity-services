package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRoleRequest(

        @NotBlank(message = "Role key is required")
        @Size(min = 2, max = 100,
                message = "Role key must be between 2 and 100 characters")
        @Pattern(
                regexp = "^[a-z0-9][a-z0-9_-]*$",
                message = "Role key may contain lowercase letters, numbers, underscore and hyphen"
        )
        String roleKey,

        @NotBlank(message = "Role name is required")
        @Size(max = 200,
                message = "Role name must not exceed 200 characters")
        String name,

        @Size(max = 1000,
                message = "Description must not exceed 1000 characters")
        String description,

        UUID tenantId
) {
}