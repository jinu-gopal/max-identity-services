package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(

        @NotBlank(message = "Tenant key is required")
        @Size(min = 2, max = 100, message = "Tenant key must be between 2 and 100 characters")
        @Pattern(
                regexp = "^[a-z0-9][a-z0-9_-]*$",
                message = "Tenant key may contain lowercase letters, numbers, underscore and hyphen"
        )
        String tenantKey,

        @NotBlank(message = "Tenant name is required")
        @Size(max = 200, message = "Tenant name must not exceed 200 characters")
        String name
) {
}