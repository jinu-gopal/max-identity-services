package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.CreateRoleRequest;
import com.max.erp.identity.dto.RoleResponse;
import com.max.erp.identity.dto.UpdateRoleRequest;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleControllerTest {

    private RoleService roleService;
    private SecurityAuthorizationGuard authorizationGuard;
    private MockMvc mockMvc;

    private UUID tenantId;
    private UUID roleId;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        roleService = mock(RoleService.class);
        authorizationGuard = mock(SecurityAuthorizationGuard.class);
        RoleController controller = new RoleController(roleService, authorizationGuard);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        tenantId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        sampleRole = new Role();
        sampleRole.setRoleKey("operations_manager");
        sampleRole.setName("Operations Manager");
        sampleRole.setDescription("Manages inventory and sales");
        sampleRole.setStatus("ACTIVE");
        ReflectionTestUtils.setField(sampleRole, "id", roleId);
        ReflectionTestUtils.setField(sampleRole, "createdAt", Instant.now());
        ReflectionTestUtils.setField(sampleRole, "updatedAt", Instant.now());
    }

    @Test
    @DisplayName("GET /api/v1/roles returns 200 with role list")
    void getRolesReturns200() throws Exception {
        when(roleService.getTenantRoles(tenantId)).thenReturn(List.of(sampleRole));

        mockMvc.perform(get("/api/v1/roles")
                .param("tenantId", tenantId.toString())
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(roleId.toString()))
                .andExpect(jsonPath("$[0].roleKey").value("operations_manager"))
                .andExpect(jsonPath("$[0].name").value("Operations Manager"));

        verify(authorizationGuard).require(any(), eq(tenantId), isNull(), eq("SECURITY"), eq("ROLE_MANAGE"));
    }

    @Test
    @DisplayName("GET /api/v1/roles/{roleId} returns 200 with role detail")
    void getRoleReturns200() throws Exception {
        when(roleService.getRole(roleId)).thenReturn(sampleRole);

        mockMvc.perform(get("/api/v1/roles/{roleId}", roleId)
                .param("tenantId", tenantId.toString())
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.roleKey").value("operations_manager"));
    }

    @Test
    @DisplayName("POST /api/v1/roles creates role and returns 200")
    void createRoleReturns200() throws Exception {
        when(roleService.createRole(any(CreateRoleRequest.class))).thenReturn(sampleRole);

        mockMvc.perform(post("/api/v1/roles")
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "tenantId": "%s",
                            "roleKey": "operations_manager",
                            "name": "Operations Manager",
                            "description": "Manages operations"
                        }
                        """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleKey").value("operations_manager"));
    }
}
