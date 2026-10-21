package com.max.erp.identity.dto;

import java.util.Map;

public class EvaluateRulesRequest {
    private String businessObjectId;
    private String businessObjectKey;
    private String triggerEvent; // ON_CHANGE, ON_SUBMIT, ON_LOAD
    private Map<String, Object> recordData;

    public EvaluateRulesRequest() {}

    public EvaluateRulesRequest(String businessObjectId, String businessObjectKey, String triggerEvent, Map<String, Object> recordData) {
        this.businessObjectId = businessObjectId;
        this.businessObjectKey = businessObjectKey;
        this.triggerEvent = triggerEvent;
        this.recordData = recordData;
    }

    public String getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(String businessObjectId) { this.businessObjectId = businessObjectId; }

    public String getBusinessObjectKey() { return businessObjectKey; }
    public void setBusinessObjectKey(String businessObjectKey) { this.businessObjectKey = businessObjectKey; }

    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }

    public Map<String, Object> getRecordData() { return recordData; }
    public void setRecordData(Map<String, Object> recordData) { this.recordData = recordData; }
}
