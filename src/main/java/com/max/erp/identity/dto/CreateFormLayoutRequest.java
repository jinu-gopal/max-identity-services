package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class CreateFormLayoutRequest {

    @NotBlank(message = "Layout key is required")
    private String layoutKey;

    @NotBlank(message = "Layout name is required")
    private String name;

    private String businessObjectId;
    private String businessObjectName;
    private String assignedRole = "ALL";
    private boolean isDefault = true;
    private String headerSubtitleField;
    private List<String> headerHighlightFields = new ArrayList<>();
    private List<FormLayoutSectionDto> sections = new ArrayList<>();
    private List<String> relatedLists = new ArrayList<>();

    public CreateFormLayoutRequest() {}

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
}
