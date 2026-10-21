package com.max.erp.identity.controller;

import com.max.erp.identity.authorization.AuthorizationContext;
import com.max.erp.identity.authorization.AuthorizationDecision;
import com.max.erp.identity.authorization.AuthorizationException;
import com.max.erp.identity.authorization.AuthorizationService;
import com.max.erp.identity.authorization.SecurityAuthorizationGuard;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.exception.ApiErrorResponse;
import com.max.erp.identity.exception.GlobalExceptionHandler;
import com.max.erp.identity.repository.UserRepository;
import com.max.erp.identity.repository.UserRoleRepository;
import com.max.erp.identity.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfigurationSecurityIntegrationTest {

    private AuthorizationService authorizationService;
    private SecurityAuthorizationGuard authorizationGuard;
    private GlobalExceptionHandler exceptionHandler;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        authorizationService = mock(AuthorizationService.class);
        authorizationGuard = new SecurityAuthorizationGuard(authorizationService);
        exceptionHandler = new GlobalExceptionHandler();
        securityConfig = new SecurityConfig();
    }

    @Test
    void unauthenticatedRequestShouldThrowAuthenticationRequired() {
        UUID tenantId = UUID.randomUUID();

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> authorizationGuard.require(null, tenantId, null, "CONFIGURATION", "READ")
        );

        assertEquals(AuthorizationDecision.PERMISSION_DENIED.name(), exception.getCode());
        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void unauthenticatedRequestWithIsAuthenticatedFalseShouldThrow() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> authorizationGuard.require(auth, UUID.randomUUID(), null, "CONFIGURATION", "READ")
        );

        assertEquals(AuthorizationDecision.PERMISSION_DENIED.name(), exception.getCode());
        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void unauthenticatedRequestWithBlankUsernameShouldThrow() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("   ");

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> authorizationGuard.require(auth, UUID.randomUUID(), null, "CONFIGURATION", "READ")
        );

        assertEquals(AuthorizationDecision.PERMISSION_DENIED.name(), exception.getCode());
        assertEquals("Authenticated user could not be resolved", exception.getMessage());
    }

    @Test
    void unauthorizedUserWithoutPermissionShouldThrowPermissionDenied() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("unauthorized_user");

        UUID userId = UUID.randomUUID();
        when(authorizationService.resolveUserId("unauthorized_user")).thenReturn(userId);

        doThrow(new AuthorizationException(
                AuthorizationDecision.PERMISSION_DENIED.name(),
                "User does not have the required permission"
        )).when(authorizationService).require(any());

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> authorizationGuard.require(auth, UUID.randomUUID(), null, "CONFIGURATION", "MANAGE")
        );

        assertEquals("PERMISSION_DENIED", exception.getCode());
        assertEquals("User does not have the required permission", exception.getMessage());
    }

    @Test
    void unauthenticatedRequestWithAnonymousUserShouldThrow() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("anonymousUser");

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> authorizationGuard.require(auth, UUID.randomUUID(), null, "CONFIGURATION", "READ")
        );

        assertEquals(AuthorizationDecision.PERMISSION_DENIED.name(), exception.getCode());
        assertEquals("Authenticated user could not be resolved", exception.getMessage());
    }

    @Test
    void authorizedUserPassesAuthorizationGuard() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("authorized_admin");

        UUID userId = UUID.randomUUID();
        when(authorizationService.resolveUserId("authorized_admin")).thenReturn(userId);

        doNothing().when(authorizationService).require(any());

        assertDoesNotThrow(() ->
                authorizationGuard.require(auth, UUID.randomUUID(), null, "CONFIGURATION", "MANAGE")
        );
    }

    @Test
    void realAuthorizationServiceAllowsAdminWithoutTenantIdForConfigurationManage() {
        UserRepository userRepository = mock(UserRepository.class);
        UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
        AuthorizationService realService = new AuthorizationService(
                null, null, null, null, null, userRepository, userRoleRepository
        );

        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setUsername("admin");
        admin.setStatus("ACTIVE");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        SecurityAuthorizationGuard realGuard = new SecurityAuthorizationGuard(realService);
        Authentication adminAuth = mock(Authentication.class);
        when(adminAuth.isAuthenticated()).thenReturn(true);
        when(adminAuth.getName()).thenReturn("admin");

        assertDoesNotThrow(() ->
                realGuard.require(adminAuth, null, null, "CONFIGURATION", "MANAGE")
        );

        AuthorizationDecision decision = realService.evaluate(
                new AuthorizationContext(adminId, null, null, "CONFIGURATION", "MANAGE")
        );
        assertEquals(AuthorizationDecision.ALLOW, decision);
    }

    @Test
    void realAuthorizationServiceDeniesRegularUserWithoutTenantId() {
        UserRepository userRepository = mock(UserRepository.class);
        UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
        AuthorizationService realService = new AuthorizationService(
                null, null, null, null, null, userRepository, userRoleRepository
        );

        UUID regularUserId = UUID.randomUUID();
        User regularUser = new User();
        regularUser.setId(regularUserId);
        regularUser.setUsername("regular_user");
        regularUser.setStatus("ACTIVE");

        when(userRepository.findByUsername("regular_user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(regularUserId)).thenReturn(Optional.of(regularUser));
        when(userRoleRepository.findAllByUserId(regularUserId)).thenReturn(List.of());

        SecurityAuthorizationGuard realGuard = new SecurityAuthorizationGuard(realService);
        Authentication regularAuth = mock(Authentication.class);
        when(regularAuth.isAuthenticated()).thenReturn(true);
        when(regularAuth.getName()).thenReturn("regular_user");

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> realGuard.require(regularAuth, null, null, "CONFIGURATION", "MANAGE")
        );

        assertEquals("PERMISSION_DENIED", exception.getCode());
        assertEquals("User does not have the required permission", exception.getMessage());
    }

    @Test
    void realAuthorizationServiceDeniesInactiveAdmin() {
        UserRepository userRepository = mock(UserRepository.class);
        UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
        AuthorizationService realService = new AuthorizationService(
                null, null, null, null, null, userRepository, userRoleRepository
        );

        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setUsername("admin");
        admin.setStatus("SUSPENDED");

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        AuthorizationDecision decision = realService.evaluate(
                new AuthorizationContext(adminId, null, null, "CONFIGURATION", "MANAGE")
        );
        assertEquals(AuthorizationDecision.PERMISSION_DENIED, decision);
    }

    @Test
    void globalExceptionHandlerMapsAuthorizationExceptionTo403Forbidden() {
        AuthorizationException exception = new AuthorizationException(
                "PERMISSION_DENIED",
                "User does not have the required permission"
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/configuration/business-objects");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAuthorization(exception, request);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("PERMISSION_DENIED", response.getBody().code());
        assertEquals("User does not have the required permission", response.getBody().message());
        assertEquals("/api/v1/configuration/business-objects", response.getBody().path());
    }

    @Test
    void corsConfigurationProtectsAllowedOriginsAndHeaders() {
        var source = securityConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/configuration/business-objects");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertNotNull(config);

        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(config.getAllowCredentials());
        assertTrue(config.getAllowedHeaders().contains("Authorization"));
        assertTrue(config.getAllowedHeaders().contains("Content-Type"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("PUT"));
        assertTrue(config.getAllowedMethods().contains("DELETE"));
    }
}
