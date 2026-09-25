package com.max.erp.identity.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BusinessObjectFieldResponse(
        UUID id,
        UUID businessObjectId,
        String fieldKey,
        String label,
        String description,
        String fieldType,
        boolean required,
        boolean uniqueValue,
        boolean readOnly,
        boolean hidden,
        String defaultValue,
        BigDecimal minValue,
        BigDecimal maxValue,
        String validationPattern,
        int displayOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}