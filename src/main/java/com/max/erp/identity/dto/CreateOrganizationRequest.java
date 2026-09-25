package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrganizationRequest(

        @NotBlank(message = "Organization key is required")
        @Size(min = 2, max = 100, message = "Organization key must be between 2 and 100 characters")
        String organizationKey,

        @NotBlank(message = "Organization name is required")
        @Size(max = 200, message = "Organization name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Organization type is required")
        String organizationType,

        UUID parentOrganizationId
) {
}