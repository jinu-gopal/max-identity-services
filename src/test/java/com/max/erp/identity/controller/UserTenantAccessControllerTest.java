package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.dto.UserTenantAccessResponse;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.entity.UserTenantAccess;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.service.UserTenantAccessService;
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

class UserTenantAccessControllerTest {

    private UserTenantAccessService userTenantAccessService;
    private SecurityAuthorizationGuard authorizationGuard;
    private MockMvc mockMvc;

    private UUID userId;
    private UUID tenantId;
    private UserTenantAccess sampleAccess;

    @BeforeEach
    void setUp() {
        userTenantAccessService = mock(UserTenantAccessService.class);
        authorizationGuard = mock(SecurityAuthorizationGuard.class);
        UserTenantAccessController controller = new UserTenantAccessController(userTenantAccessService, authorizationGuard);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        Tenant tenant = new Tenant();
        tenant.setTenantKey("security-test");
        tenant.setName("MAX Security Test Tenant");
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        sampleAccess = new UserTenantAccess();
        sampleAccess.setUser(user);
        sampleAccess.setTenant(tenant);
        sampleAccess.setStatus("ACTIVE");
        ReflectionTestUtils.setField(sampleAccess, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(sampleAccess, "createdAt", Instant.now());
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId}/tenant-access returns 200 with tenant access list")
    void getUserTenantAccessReturns200() throws Exception {
        when(userTenantAccessService.getUserAccess(userId)).thenReturn(List.of(sampleAccess));

        mockMvc.perform(get("/api/v1/users/{userId}/tenant-access", userId)
                .param("tenantId", tenantId.toString())
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantKey").value("security-test"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(authorizationGuard).require(any(), eq(tenantId), isNull(), eq("SECURITY"), eq("ACCESS_MANAGE"));
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/tenant-access grants tenant access and returns 200")
    void grantTenantAccessReturns200() throws Exception {
        when(userTenantAccessService.grantAccess(eq(userId), eq(tenantId), isNull())).thenReturn(sampleAccess);

        mockMvc.perform(post("/api/v1/users/{userId}/tenant-access", userId)
                .principal(new TestingAuthenticationToken("admin", "pass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "tenantId": "%s"
                        }
                        """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantKey").value("security-test"));
    }
}
