package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class TogglePermissionRequest {

    @NotNull
    private UUID roleId;

    @NotNull
    private UUID permissionId;

    private boolean grant;

    public TogglePermissionRequest() {}

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    public boolean isGrant() {
        return grant;
    }

    public void setGrant(boolean grant) {
        this.grant = grant;
    }
}
