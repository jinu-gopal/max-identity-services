package com.max.erp.identity.controller;

import com.max.erp.identity.entity.Permission;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.PermissionService;
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

class PermissionControllerTest {

    private PermissionService permissionService;
    private MockMvc mockMvc;

    private UUID permissionId;
    private Permission samplePermission;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        PermissionController controller = new PermissionController(permissionService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        permissionId = UUID.randomUUID();
        samplePermission = new Permission();
        samplePermission.setPermissionKey("SECURITY.ROLE_MANAGE");
        samplePermission.setName("Manage Roles");
        samplePermission.setModuleKey("SECURITY");
        samplePermission.setActionKey("ROLE_MANAGE");
        ReflectionTestUtils.setField(samplePermission, "id", permissionId);
        ReflectionTestUtils.setField(samplePermission, "createdAt", Instant.now());
        ReflectionTestUtils.setField(samplePermission, "updatedAt", Instant.now());
    }

    @Test
    @DisplayName("GET /api/v1/permissions returns 200 with permission list")
    void getAllPermissionsReturns200() throws Exception {
        when(permissionService.getAllPermissions()).thenReturn(List.of(samplePermission));

        mockMvc.perform(get("/api/v1/permissions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(permissionId.toString()))
                .andExpect(jsonPath("$[0].permissionKey").value("SECURITY.ROLE_MANAGE"))
                .andExpect(jsonPath("$[0].moduleKey").value("SECURITY"));
    }

    @Test
    @DisplayName("GET /api/v1/permissions/{id} returns 200 with permission detail")
    void getPermissionByIdReturns200() throws Exception {
        when(permissionService.getPermission(permissionId)).thenReturn(samplePermission);

        mockMvc.perform(get("/api/v1/permissions/{permissionId}", permissionId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(permissionId.toString()))
                .andExpect(jsonPath("$.actionKey").value("ROLE_MANAGE"));
    }
}
