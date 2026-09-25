package com.max.erp.identity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBusinessObjectFieldOptionRequest(

        @NotBlank(message = "Option value is required")
        @Size(max = 200, message = "Option value cannot exceed 200 characters")
        String optionValue,

        @NotBlank(message = "Option label is required")
        @Size(max = 200, message = "Option label cannot exceed 200 characters")
        String optionLabel,

        @Min(value = 0, message = "Display order cannot be negative")
        int displayOrder,

        boolean active,

        boolean defaultOption
) {
}