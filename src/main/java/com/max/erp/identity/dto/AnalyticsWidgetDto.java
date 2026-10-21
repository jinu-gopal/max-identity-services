package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyticsWidgetDto {
    private String id;
    private String title;
    private String widgetType; // METRIC_CARD, BAR_CHART, LINE_AREA, DONUT_PIE, RECENT_TRANSACTIONS
    private String dataMetric;
    private String value;
    private String trend;
    private String timeRange;
    private List<Map<String, Object>> chartData = new ArrayList<>();
    private int colSpan = 1;

    public AnalyticsWidgetDto() {}

    public AnalyticsWidgetDto(String id, String title, String widgetType, String dataMetric,
                              String value, String trend, String timeRange,
                              List<Map<String, Object>> chartData, int colSpan) {
        this.id = id;
        this.title = title;
        this.widgetType = widgetType;
        this.dataMetric = dataMetric;
        this.value = value;
        this.trend = trend;
        this.timeRange = timeRange;
        this.chartData = chartData != null ? chartData : new ArrayList<>();
        this.colSpan = colSpan;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getWidgetType() { return widgetType; }
    public void setWidgetType(String widgetType) { this.widgetType = widgetType; }

    public String getDataMetric() { return dataMetric; }
    public void setDataMetric(String dataMetric) { this.dataMetric = dataMetric; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }

    public int getColSpan() { return colSpan; }
    public void setColSpan(int colSpan) { this.colSpan = colSpan; }
}
