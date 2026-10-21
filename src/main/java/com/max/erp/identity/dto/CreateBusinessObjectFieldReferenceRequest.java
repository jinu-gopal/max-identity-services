package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBusinessObjectFieldReferenceRequest(
        @NotNull UUID targetBusinessObjectId,
        UUID displayFieldId,
        boolean allowMultiple,
        boolean allowCreate
) {
}