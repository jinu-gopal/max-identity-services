package com.max.erp.identity.dto;

import com.max.erp.identity.entity.RolePermission;

import java.time.Instant;
import java.util.UUID;

public record RolePermissionResponse(
        UUID id,
        UUID roleId,
        String roleKey,
        UUID permissionId,
        String permissionKey,
        String permissionName,
        String moduleKey,
        String actionKey,
        Instant createdAt
) {

    public static RolePermissionResponse from(RolePermission rp) {
        if (rp == null) {
            return null;
        }

        UUID rId = null;
        String rKey = null;
        if (rp.getRole() != null) {
            try {
                rId = rp.getRole().getId();
                rKey = rp.getRole().getRoleKey();
            } catch (Exception ignored) {
                // lazy proxy fallback
            }
        }

        UUID pId = null;
        String pKey = null;
        String pName = null;
        String mKey = null;
        String aKey = null;
        if (rp.getPermission() != null) {
            try {
                pId = rp.getPermission().getId();
                pKey = rp.getPermission().getPermissionKey();
                pName = rp.getPermission().getName();
                mKey = rp.getPermission().getModuleKey();
                aKey = rp.getPermission().getActionKey();
            } catch (Exception ignored) {
                // lazy proxy fallback
            }
        }

        return new RolePermissionResponse(
                rp.getId(),
                rId,
                rKey,
                pId,
                pKey,
                pName,
                mKey,
                aKey,
                rp.getCreatedAt()
        );
    }
}
