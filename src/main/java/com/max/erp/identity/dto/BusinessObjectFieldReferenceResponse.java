package com.max.erp.identity.dto;

import java.util.UUID;

public record BusinessObjectFieldReferenceResponse(
        UUID id,
        UUID fieldId,
        UUID targetBusinessObjectId,
        UUID displayFieldId,
        boolean allowMultiple,
        boolean allowCreate
) {
}