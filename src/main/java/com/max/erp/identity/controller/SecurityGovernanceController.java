package com.max.erp.identity.controller;

import com.max.erp.identity.dto.*;
import com.max.erp.identity.service.SecurityGovernanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityGovernanceController {

    private final SecurityGovernanceService securityGovernanceService;

    public SecurityGovernanceController(SecurityGovernanceService securityGovernanceService) {
        this.securityGovernanceService = securityGovernanceService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SecurityDashboardDto> getDashboardMetrics(
            @RequestParam(required = false) UUID tenantId
    ) {
        SecurityDashboardDto metrics = securityGovernanceService.getDashboardMetrics(tenantId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/audit-trail")
    public ResponseEntity<List<AuditTrailEventDto>> getAuditTrail(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        List<AuditTrailEventDto> events = securityGovernanceService.getUnifiedAuditTrail(tenantId, category, search, limit);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveSessionDto>> getActiveSessions() {
        List<ActiveSessionDto> sessions = securityGovernanceService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> terminateSession(@PathVariable String sessionId) {
        boolean success = securityGovernanceService.terminateSession(sessionId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", "Session " + sessionId + " terminated successfully."
        ));
    }

    @GetMapping("/matrix")
    public ResponseEntity<RbacMatrixDto> getRbacMatrix(
            @RequestParam(required = false) UUID tenantId
    ) {
        RbacMatrixDto matrix = securityGovernanceService.getRbacMatrix(tenantId);
        return ResponseEntity.ok(matrix);
    }

    @PostMapping("/matrix/toggle")
    public ResponseEntity<Map<String, Object>> togglePermission(
            @Valid @RequestBody TogglePermissionRequest request
    ) {
        boolean success = securityGovernanceService.toggleRolePermission(request);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "roleId", request.getRoleId(),
                "permissionId", request.getPermissionId(),
                "granted", request.isGrant()
        ));
    }
}
