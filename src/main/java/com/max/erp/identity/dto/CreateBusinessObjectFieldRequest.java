package com.max.erp.identity.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateBusinessObjectFieldRequest(

        @NotBlank
        @Size(max = 100)
        @Pattern(
                regexp = "^[a-z][a-z0-9_]*$",
                message = "fieldKey must start with a lowercase letter and contain only lowercase letters, numbers and underscores"
        )
        String fieldKey,

        @NotBlank
        @Size(max = 150)
        String label,

        @Size(max = 1000)
        String description,

        @NotBlank
        @Size(max = 50)
        String fieldType,

        boolean required,

        boolean uniqueValue,

        boolean readOnly,

        boolean hidden,

        @Size(max = 2000)
        String defaultValue,

        BigDecimal minValue,

        BigDecimal maxValue,

        @Size(max = 1000)
        String validationPattern,

        @Min(0)
        Integer displayOrder
) {
}