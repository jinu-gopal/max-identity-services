package com.max.erp.identity.controller;

import com.max.erp.identity.dto.AnalyticsDashboardDto;
import com.max.erp.identity.dto.CreateAnalyticsDashboardRequest;
import com.max.erp.identity.service.EnterpriseAnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class EnterpriseAnalyticsController {

    private final EnterpriseAnalyticsService analyticsService;

    public EnterpriseAnalyticsController(EnterpriseAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboards")
    public ResponseEntity<List<AnalyticsDashboardDto>> getAllDashboards() {
        return ResponseEntity.ok(analyticsService.getAllDashboards());
    }

    @GetMapping("/dashboards/{id}")
    public ResponseEntity<AnalyticsDashboardDto> getDashboardById(@PathVariable String id) {
        AnalyticsDashboardDto dash = analyticsService.getDashboardById(id);
        if (dash == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dash);
    }

    @PostMapping("/dashboards")
    public ResponseEntity<AnalyticsDashboardDto> createDashboard(
            @Valid @RequestBody CreateAnalyticsDashboardRequest request
    ) {
        AnalyticsDashboardDto created = analyticsService.createDashboard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/dashboards/{id}")
    public ResponseEntity<AnalyticsDashboardDto> updateDashboard(
            @PathVariable String id,
            @Valid @RequestBody CreateAnalyticsDashboardRequest request
    ) {
        AnalyticsDashboardDto updated = analyticsService.updateDashboard(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/dashboards/{id}")
    public ResponseEntity<Void> deleteDashboard(@PathVariable String id) {
        boolean deleted = analyticsService.deleteDashboard(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryAnalytics(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(analyticsService.queryAnalytics(params));
    }
}
