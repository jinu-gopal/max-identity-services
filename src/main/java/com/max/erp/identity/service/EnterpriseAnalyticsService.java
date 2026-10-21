package com.max.erp.identity.service;

import com.max.erp.identity.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EnterpriseAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(EnterpriseAnalyticsService.class);

    private final Map<String, AnalyticsDashboardDto> dashboardsMap = new ConcurrentHashMap<>();

    public EnterpriseAnalyticsService() {
        initializeDefaultDashboards();
    }

    private void initializeDefaultDashboards() {
        // Dashboard 1: Executive Overview
        AnalyticsWidgetDto w1 = new AnalyticsWidgetDto(
                "w-rev-kpi",
                "Gross Recognized Revenue",
                "METRIC_CARD",
                "revenue",
                "$4,850,200",
                "+18.4% vs last quarter",
                "30D",
                Collections.emptyList(),
                1
        );

        AnalyticsWidgetDto w2 = new AnalyticsWidgetDto(
                "w-cust-kpi",
                "Active Enterprise Accounts",
                "METRIC_CARD",
                "accounts",
                "1,420",
                "+8.2% new onboardings",
                "30D",
                Collections.emptyList(),
                1
        );

        AnalyticsWidgetDto w3 = new AnalyticsWidgetDto(
                "w-sla-kpi",
                "Workflow Approval SLA Compliance",
                "METRIC_CARD",
                "sla",
                "99.4%",
                "+1.1% on-time approvals",
                "30D",
                Collections.emptyList(),
                1
        );

        AnalyticsWidgetDto w4 = new AnalyticsWidgetDto(
                "w-ingest-kpi",
                "Data Pipeline Throughput",
                "METRIC_CARD",
                "throughput",
                "18,450 rows",
                "99.13% zero-fault rate",
                "30D",
                Collections.emptyList(),
                1
        );

        AnalyticsWidgetDto w5 = new AnalyticsWidgetDto(
                "w-rev-chart",
                "Monthly Revenue Trajectory (USD Millions)",
                "BAR_CHART",
                "revenue_trend",
                "$4.85M",
                "+24% YoY",
                "YTD",
                List.of(
                        Map.of("label", "Jan", "value", 320),
                        Map.of("label", "Feb", "value", 380),
                        Map.of("label", "Mar", "value", 450),
                        Map.of("label", "Apr", "value", 410),
                        Map.of("label", "May", "value", 520),
                        Map.of("label", "Jun", "value", 590),
                        Map.of("label", "Jul", "value", 610),
                        Map.of("label", "Aug", "value", 680),
                        Map.of("label", "Sep", "value", 720),
                        Map.of("label", "Oct", "value", 850)
                ),
                2
        );

        AnalyticsWidgetDto w6 = new AnalyticsWidgetDto(
                "w-dept-donut",
                "Budget Allocation by Business Unit",
                "DONUT_PIE",
                "budget_distribution",
                "100%",
                "Full allocation",
                "YTD",
                List.of(
                        Map.of("label", "Engineering & R&D", "value", 34, "color", "#3b82f6"),
                        Map.of("label", "Operations & Supply", "value", 28, "color", "#10b981"),
                        Map.of("label", "Sales & Marketing", "value", 22, "color", "#f59e0b"),
                        Map.of("label", "Corporate & G&A", "value", 16, "color", "#8b5cf6")
                ),
                2
        );

        AnalyticsDashboardDto d1 = new AnalyticsDashboardDto(
                "dash-exec-overview",
                "Executive Performance Command Center",
                "High-level enterprise revenue, accounts growth, approval SLAs, and budget distribution",
                "EXECUTIVE",
                true,
                List.of(w1, w2, w3, w4, w5, w6),
                Instant.now().minusSeconds(86400)
        );
        dashboardsMap.put(d1.getId(), d1);

        // Dashboard 2: Sales & Commercial Performance
        AnalyticsDashboardDto d2 = new AnalyticsDashboardDto(
                "dash-sales-commercial",
                "Sales & Commercial Operations",
                "Order pipeline velocity, customer account tiers, and contract fulfillment",
                "SALES",
                false,
                List.of(w1, w2, w5),
                Instant.now().minusSeconds(43200)
        );
        dashboardsMap.put(d2.getId(), d2);
    }

    public List<AnalyticsDashboardDto> getAllDashboards() {
        return dashboardsMap.values().stream()
                .sorted(Comparator.comparing(AnalyticsDashboardDto::isDefault).reversed())
                .collect(Collectors.toList());
    }

    public AnalyticsDashboardDto getDashboardById(String id) {
        return dashboardsMap.get(id);
    }

    public AnalyticsDashboardDto createDashboard(CreateAnalyticsDashboardRequest req) {
        String id = "dash-" + UUID.randomUUID().toString().substring(0, 8);
        AnalyticsDashboardDto dto = new AnalyticsDashboardDto(
                id,
                req.getName(),
                req.getDescription(),
                req.getCategory() != null ? req.getCategory().toUpperCase() : "GENERAL",
                req.isDefault(),
                req.getWidgets(),
                Instant.now()
        );
        dashboardsMap.put(id, dto);
        log.info("Created analytics dashboard: {} ({})", dto.getName(), dto.getId());
        return dto;
    }

    public AnalyticsDashboardDto updateDashboard(String id, CreateAnalyticsDashboardRequest req) {
        AnalyticsDashboardDto existing = dashboardsMap.get(id);
        if (existing == null) return null;

        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        if (req.getCategory() != null) existing.setCategory(req.getCategory().toUpperCase());
        existing.setDefault(req.isDefault());
        if (req.getWidgets() != null) existing.setWidgets(req.getWidgets());
        existing.setUpdatedAt(Instant.now());

        return existing;
    }

    public boolean deleteDashboard(String id) {
        return dashboardsMap.remove(id) != null;
    }

    public Map<String, Object> queryAnalytics(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("queriedAt", Instant.now().toString());
        result.put("sampleSize", 18450);
        result.put("status", "SUCCESS");
        result.put("metrics", Map.of(
                "totalRevenue", 4850200.00,
                "orderCount", 1240,
                "averageOrderValue", 3911.45,
                "slaAdherence", 99.4
        ));
        return result;
    }
}
