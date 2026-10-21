package com.max.erp.identity.dto;

import java.util.HashMap;
import java.util.Map;

public class RenderDocumentRequest {
    private String templateId;
    private String recordId;
    private Map<String, Object> contextData = new HashMap<>();

    public RenderDocumentRequest() {}

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public Map<String, Object> getContextData() { return contextData; }
    public void setContextData(Map<String, Object> contextData) { this.contextData = contextData; }
}
