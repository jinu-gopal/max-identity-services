package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateBusinessObjectFieldReferenceRequest(
        @NotNull UUID targetBusinessObjectId,
        UUID displayFieldId,
        boolean allowMultiple,
        boolean allowCreate
) {
}