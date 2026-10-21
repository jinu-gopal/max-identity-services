package com.max.erp.identity.dto;

import java.util.List;
import java.util.UUID;

public class ExportRequest {
    private UUID businessObjectId;
    private String businessObjectKey;
    private String format = "CSV"; // CSV, JSON
    private List<String> selectedFields;

    public ExportRequest() {}

    public UUID getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(UUID businessObjectId) { this.businessObjectId = businessObjectId; }

    public String getBusinessObjectKey() { return businessObjectKey; }
    public void setBusinessObjectKey(String businessObjectKey) { this.businessObjectKey = businessObjectKey; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public List<String> getSelectedFields() { return selectedFields; }
    public void setSelectedFields(List<String> selectedFields) { this.selectedFields = selectedFields; }
}
