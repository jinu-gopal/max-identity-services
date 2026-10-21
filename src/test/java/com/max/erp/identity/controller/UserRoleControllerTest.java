package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.UserRoleResponse;
import com.max.erp.identity.entity.Role;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.entity.UserRole;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.UserRoleService;
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

class UserRoleControllerTest {

    private UserRoleService userRoleService;
    private SecurityAuthorizationGuard authorizationGuard;
    private MockMvc mockMvc;

    private UUID userId;
    private UUID roleId;
    private UUID tenantId;
    private UserRole sampleUserRole;

    @BeforeEach
    void setUp() {
        userRoleService = mock(UserRoleService.class);
        authorizationGuard = mock(SecurityAuthorizationGuard.class);
        UserRoleController controller = new UserRoleController(userRoleService, authorizationGuard);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        Role role = new Role();
        role.setRoleKey("security_test_manager");
        role.setName("Security Test Manager");
        ReflectionTestUtils.setField(role, "id", roleId);

        Tenant tenant = new Tenant();
        tenant.setTenantKey("security-test");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        sampleUserRole = new UserRole();
        sampleUserRole.setUser(user);
        sampleUserRole.setRole(role);
        sampleUserRole.setTenant(tenant);
        sampleUserRole.setStatus("ACTIVE");
        ReflectionTestUtils.setField(sampleUserRole, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(sampleUserRole, "createdAt", Instant.now());
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/roles returns 200 with user roles")
    void getUserRolesReturns200() throws Exception {
        when(userRoleService.getUserRoles(userId)).thenReturn(List.of(sampleUserRole));

        mockMvc.perform(get("/api/v1/users/{userId}/roles", userId)
                .param("tenantId", tenantId.toString())
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleKey").value("security_test_manager"))
                .andExpect(jsonPath("$[0].roleName").value("Security Test Manager"));

        verify(authorizationGuard).require(any(), eq(tenantId), isNull(), eq("SECURITY"), eq("USER_ROLE_ASSIGN"));
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/roles assigns role and returns 200")
    void assignRoleReturns200() throws Exception {
        when(userRoleService.assignRole(eq(userId), eq(roleId), eq(tenantId), isNull())).thenReturn(sampleUserRole);

        mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "roleId": "%s",
                            "tenantId": "%s"
                        }
                        """.formatted(roleId, tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleKey").value("security_test_manager"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{userId}/roles/{roleId} revokes role and returns 204")
    void revokeRoleReturns204() throws Exception {
        doNothing().when(userRoleService).revokeRole(userId, roleId, tenantId, null);

        mockMvc.perform(delete("/api/v1/users/{userId}/roles/{roleId}", userId, roleId)
                .param("tenantId", tenantId.toString())
                .principal(new TestingAuthenticationToken("admin", "pass")))
                .andExpect(status().isNoContent());

        verify(userRoleService).revokeRole(userId, roleId, tenantId, null);
    }
}
