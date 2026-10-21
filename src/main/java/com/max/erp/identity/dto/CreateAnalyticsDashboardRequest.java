package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class CreateAnalyticsDashboardRequest {

    @NotBlank(message = "Dashboard name is required")
    private String name;

    private String description;
    private String category = "EXECUTIVE";
    private boolean isDefault = false;
    private List<AnalyticsWidgetDto> widgets = new ArrayList<>();

    public CreateAnalyticsDashboardRequest() {}

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
}
