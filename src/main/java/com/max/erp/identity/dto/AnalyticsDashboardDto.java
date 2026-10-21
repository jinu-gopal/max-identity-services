package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDashboardDto {
    private String id;
    private String name;
    private String description;
    private String category; // EXECUTIVE, SALES, OPERATIONS, GOVERNANCE
    private boolean isDefault;
    private List<AnalyticsWidgetDto> widgets = new ArrayList<>();
    private Instant updatedAt;

    public AnalyticsDashboardDto() {}

    public AnalyticsDashboardDto(String id, String name, String description, String category,
                                 boolean isDefault, List<AnalyticsWidgetDto> widgets, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.isDefault = isDefault;
        this.widgets = widgets != null ? widgets : new ArrayList<>();
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public List<AnalyticsWidgetDto> getWidgets() { return widgets; }
    public void setWidgets(List<AnalyticsWidgetDto> widgets) { this.widgets = widgets; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
