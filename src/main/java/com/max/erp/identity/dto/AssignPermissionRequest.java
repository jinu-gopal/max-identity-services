package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignPermissionRequest(

        @NotNull(message = "Permission ID is required")
        UUID permissionId
) {
}