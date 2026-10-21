package com.max.erp.identity.dto;

import java.util.List;
import java.util.Map;

public class RbacMatrixDto {

    private List<RoleItemDto> roles;
    private List<PermissionItemDto> permissions;
    private Map<String, List<String>> rolePermissionMap; // roleId -> List of permissionIds
    private List<UserSummaryDto> users;

    public RbacMatrixDto() {}

    public List<RoleItemDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleItemDto> roles) {
        this.roles = roles;
    }

    public List<PermissionItemDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionItemDto> permissions) {
        this.permissions = permissions;
    }

    public Map<String, List<String>> getRolePermissionMap() {
        return rolePermissionMap;
    }

    public void setRolePermissionMap(Map<String, List<String>> rolePermissionMap) {
        this.rolePermissionMap = rolePermissionMap;
    }

    public List<UserSummaryDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserSummaryDto> users) {
        this.users = users;
    }

    public static class RoleItemDto {
        private String id;
        private String roleKey;
        private String name;
        private String description;

        public RoleItemDto() {}

        public RoleItemDto(String id, String roleKey, String name, String description) {
            this.id = id;
            this.roleKey = roleKey;
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRoleKey() {
            return roleKey;
        }

        public void setRoleKey(String roleKey) {
            this.roleKey = roleKey;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class PermissionItemDto {
        private String id;
        private String permissionKey;
        private String name;
        private String description;
        private String moduleKey;

        public PermissionItemDto() {}

        public PermissionItemDto(String id, String permissionKey, String name, String description, String moduleKey) {
            this.id = id;
            this.permissionKey = permissionKey;
            this.name = name;
            this.description = description;
            this.moduleKey = moduleKey;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPermissionKey() {
            return permissionKey;
        }

        public void setPermissionKey(String permissionKey) {
            this.permissionKey = permissionKey;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getModuleKey() {
            return moduleKey;
        }

        public void setModuleKey(String moduleKey) {
            this.moduleKey = moduleKey;
        }
    }

    public static class UserSummaryDto {
        private String id;
        private String username;
        private String email;
        private List<String> roleIds;

        public UserSummaryDto() {}

        public UserSummaryDto(String id, String username, String email, List<String> roleIds) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.roleIds = roleIds;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public List<String> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<String> roleIds) {
            this.roleIds = roleIds;
        }
    }
}
