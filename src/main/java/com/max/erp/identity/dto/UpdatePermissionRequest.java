package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(

        @NotBlank(message = "Permission name is required")
        @Size(max = 200, message = "Permission name must not exceed 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}