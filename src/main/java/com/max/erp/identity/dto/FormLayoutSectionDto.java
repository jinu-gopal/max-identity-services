package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.List;

public class FormLayoutSectionDto {
    private String id;
    private String title;
    private String description;
    private int columns; // 1, 2, or 3
    private boolean collapsible;
    private boolean defaultExpanded;
    private List<FormLayoutFieldDto> fields = new ArrayList<>();

    public FormLayoutSectionDto() {
        this.columns = 2;
        this.collapsible = true;
        this.defaultExpanded = true;
    }

    public FormLayoutSectionDto(String id, String title, String description, int columns, boolean collapsible, boolean defaultExpanded, List<FormLayoutFieldDto> fields) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.columns = columns;
        this.collapsible = collapsible;
        this.defaultExpanded = defaultExpanded;
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }

    public boolean isCollapsible() { return collapsible; }
    public void setCollapsible(boolean collapsible) { this.collapsible = collapsible; }

    public boolean isDefaultExpanded() { return defaultExpanded; }
    public void setDefaultExpanded(boolean defaultExpanded) { this.defaultExpanded = defaultExpanded; }

    public List<FormLayoutFieldDto> getFields() { return fields; }
    public void setFields(List<FormLayoutFieldDto> fields) { this.fields = fields; }
}
