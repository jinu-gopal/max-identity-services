package com.max.erp.identity.controller;

import com.max.erp.identity.dto.AssignPermissionRequest;
import com.max.erp.identity.entity.Permission;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.entity.RolePermission;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.RolePermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RolePermissionControllerTest {

    private RolePermissionService rolePermissionService;
    private MockMvc mockMvc;

    private UUID roleId;
    private UUID permissionId;
    private RolePermission sampleRolePermission;

    @BeforeEach
    void setUp() {
        rolePermissionService = mock(RolePermissionService.class);
        RolePermissionController controller = new RolePermissionController(rolePermissionService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        roleId = UUID.randomUUID();
        permissionId = UUID.randomUUID();

        Role role = new Role();
        role.setRoleKey("security_test_manager");
        ReflectionTestUtils.setField(role, "id", roleId);

        Permission permission = new Permission();
        permission.setPermissionKey("SECURITY.ROLE_MANAGE");
        permission.setName("Manage Roles");
        permission.setModuleKey("SECURITY");
        permission.setActionKey("ROLE_MANAGE");
        ReflectionTestUtils.setField(permission, "id", permissionId);

        sampleRolePermission = new RolePermission();
        sampleRolePermission.setRole(role);
        sampleRolePermission.setPermission(permission);
        ReflectionTestUtils.setField(sampleRolePermission, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(sampleRolePermission, "createdAt", Instant.now());
    }

    @Test
    @DisplayName("GET /api/v1/roles/{roleId}/permissions returns 200 with permissions list")
    void getPermissionsReturns200() throws Exception {
        when(rolePermissionService.getPermissions(roleId)).thenReturn(List.of(sampleRolePermission));

        mockMvc.perform(get("/api/v1/roles/{roleId}/permissions", roleId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].permissionKey").value("SECURITY.ROLE_MANAGE"))
                .andExpect(jsonPath("$[0].moduleKey").value("SECURITY"));
    }

    @Test
    @DisplayName("POST /api/v1/roles/{roleId}/permissions assigns permission and returns 200")
    void assignPermissionReturns200() throws Exception {
        when(rolePermissionService.assignPermission(roleId, permissionId)).thenReturn(sampleRolePermission);

        mockMvc.perform(post("/api/v1/roles/{roleId}/permissions", roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "permissionId": "%s"
                        }
                        """.formatted(permissionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionKey").value("SECURITY.ROLE_MANAGE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{roleId}/permissions/{permissionId} removes permission and returns 204")
    void removePermissionReturns204() throws Exception {
        doNothing().when(rolePermissionService).removePermission(roleId, permissionId);

        mockMvc.perform(delete("/api/v1/roles/{roleId}/permissions/{permissionId}", roleId, permissionId))
                .andExpect(status().isNoContent());

        verify(rolePermissionService).removePermission(roleId, permissionId);
    }
}
