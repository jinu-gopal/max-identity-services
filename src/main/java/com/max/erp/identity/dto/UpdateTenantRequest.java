package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(

        @NotBlank(message = "Tenant name is required")
        @Size(max = 200, message = "Tenant name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Tenant status is required")
        String status
) {
}