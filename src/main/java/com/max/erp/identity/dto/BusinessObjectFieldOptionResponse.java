package com.max.erp.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BusinessObjectFieldOptionResponse(
        UUID id,
        UUID fieldId,
        String optionValue,
        String optionLabel,
        int displayOrder,
        boolean active,
        boolean defaultOption,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}