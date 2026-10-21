package com.max.erp.identity.dto;

import java.util.Map;
import java.util.UUID;

public record ImportMappingSaveRequest(
        Map<String, UUID> mapping
) {
}
