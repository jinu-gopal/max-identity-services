package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(

        @NotBlank(message = "Organization name is required")
        @Size(max = 200, message = "Organization name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Organization status is required")
        String status
) {
}