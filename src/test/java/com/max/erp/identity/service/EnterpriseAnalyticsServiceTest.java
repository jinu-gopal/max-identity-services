package com.max.erp.identity.service;

import com.max.erp.identity.dto.AnalyticsDashboardDto;
import com.max.erp.identity.dto.AnalyticsWidgetDto;
import com.max.erp.identity.dto.CreateAnalyticsDashboardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnterpriseAnalyticsServiceTest {

    private EnterpriseAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new EnterpriseAnalyticsService();
    }

    @Test
    void testInitialSeedDashboardsPresent() {
        List<AnalyticsDashboardDto> dashboards = analyticsService.getAllDashboards();
        assertNotNull(dashboards);
        assertTrue(dashboards.size() >= 2);
    }

    @Test
    void testGetDashboardById() {
        List<AnalyticsDashboardDto> dashboards = analyticsService.getAllDashboards();
        AnalyticsDashboardDto first = dashboards.get(0);

        AnalyticsDashboardDto found = analyticsService.getDashboardById(first.getId());
        assertNotNull(found);
        assertEquals(first.getName(), found.getName());
        assertFalse(found.getWidgets().isEmpty());
    }

    @Test
    void testCreateDashboard() {
        CreateAnalyticsDashboardRequest req = new CreateAnalyticsDashboardRequest();
        req.setName("Operations Monitoring Studio");
        req.setDescription("Real-time operations cockpit");
        req.setCategory("OPERATIONS");
        req.setDefault(false);

        AnalyticsWidgetDto widget = new AnalyticsWidgetDto(
                "wgt-ops-1",
                "Total Active Shipments",
                "METRIC_CARD",
                "shipments",
                "450",
                "+5.2% vs yesterday",
                "TODAY",
                Collections.emptyList(),
                1
        );

        req.setWidgets(List.of(widget));

        AnalyticsDashboardDto created = analyticsService.createDashboard(req);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Operations Monitoring Studio", created.getName());
        assertEquals(1, created.getWidgets().size());
    }

    @Test
    void testExecuteQueryAnalytics() {
        Map<String, Object> result = analyticsService.queryAnalytics(Map.of("range", "30D"));
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue(result.containsKey("metrics"));
    }
}
