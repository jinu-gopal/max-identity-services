package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FormLayoutDto {
    private String id;
    private String layoutKey;
    private String name;
    private String businessObjectId;
    private String businessObjectName;
    private String assignedRole; // ALL, ADMIN, SALES_REP, MANAGER, AUDITOR
    private boolean isDefault;
    private String headerSubtitleField;
    private List<String> headerHighlightFields = new ArrayList<>();
    private List<FormLayoutSectionDto> sections = new ArrayList<>();
    private List<String> relatedLists = new ArrayList<>();
    private Instant updatedAt;

    public FormLayoutDto() {}

    public FormLayoutDto(String id, String layoutKey, String name, String businessObjectId,
                         String businessObjectName, String assignedRole, boolean isDefault,
                         String headerSubtitleField, List<String> headerHighlightFields,
                         List<FormLayoutSectionDto> sections, List<String> relatedLists,
                         Instant updatedAt) {
        this.id = id;
        this.layoutKey = layoutKey;
        this.name = name;
        this.businessObjectId = businessObjectId;
        this.businessObjectName = businessObjectName;
        this.assignedRole = assignedRole;
        this.isDefault = isDefault;
        this.headerSubtitleField = headerSubtitleField;
        this.headerHighlightFields = headerHighlightFields != null ? headerHighlightFields : new ArrayList<>();
        this.sections = sections != null ? sections : new ArrayList<>();
        this.relatedLists = relatedLists != null ? relatedLists : new ArrayList<>();
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLayoutKey() { return layoutKey; }
    public void setLayoutKey(String layoutKey) { this.layoutKey = layoutKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(String businessObjectId) { this.businessObjectId = businessObjectId; }

    public String getBusinessObjectName() { return businessObjectName; }
    public void setBusinessObjectName(String businessObjectName) { this.businessObjectName = businessObjectName; }

    public String getAssignedRole() { return assignedRole; }
    public void setAssignedRole(String assignedRole) { this.assignedRole = assignedRole; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public String getHeaderSubtitleField() { return headerSubtitleField; }
    public void setHeaderSubtitleField(String headerSubtitleField) { this.headerSubtitleField = headerSubtitleField; }

    public List<String> getHeaderHighlightFields() { return headerHighlightFields; }
    public void setHeaderHighlightFields(List<String> headerHighlightFields) { this.headerHighlightFields = headerHighlightFields; }

    public List<FormLayoutSectionDto> getSections() { return sections; }
    public void setSections(List<FormLayoutSectionDto> sections) { this.sections = sections; }

    public List<String> getRelatedLists() { return relatedLists; }
    public void setRelatedLists(List<String> relatedLists) { this.relatedLists = relatedLists; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
