package com.max.erp.identity.dto;

import java.util.Map;
import java.util.UUID;

public record ImportMappingSaveResponse(
        UUID importJobId,
        UUID businessObjectId,
        Map<String, UUID> mapping
) {
}
