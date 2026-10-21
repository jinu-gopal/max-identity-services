package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.Map;

public class BusinessRuleDto {
    private String id;
    private String ruleKey;
    private String name;
    private String businessObjectId;
    private String businessObjectName;
    private String ruleType; // VALIDATION, VISIBILITY, COMPUTATION, LOCKING
    private String triggerEvent; // ON_CHANGE, ON_SUBMIT, ON_LOAD
    private String conditionExpression;
    private Map<String, Object> actionPayload;
    private String status; // ACTIVE, INACTIVE
    private int priorityOrder;
    private Instant createdAt;

    public BusinessRuleDto() {}

    public BusinessRuleDto(String id, String ruleKey, String name, String businessObjectId,
                           String businessObjectName, String ruleType, String triggerEvent,
                           String conditionExpression, Map<String, Object> actionPayload,
                           String status, int priorityOrder, Instant createdAt) {
        this.id = id;
        this.ruleKey = ruleKey;
        this.name = name;
        this.businessObjectId = businessObjectId;
        this.businessObjectName = businessObjectName;
        this.ruleType = ruleType;
        this.triggerEvent = triggerEvent;
        this.conditionExpression = conditionExpression;
        this.actionPayload = actionPayload;
        this.status = status;
        this.priorityOrder = priorityOrder;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRuleKey() { return ruleKey; }
    public void setRuleKey(String ruleKey) { this.ruleKey = ruleKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(String businessObjectId) { this.businessObjectId = businessObjectId; }

    public String getBusinessObjectName() { return businessObjectName; }
    public void setBusinessObjectName(String businessObjectName) { this.businessObjectName = businessObjectName; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }

    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }

    public Map<String, Object> getActionPayload() { return actionPayload; }
    public void setActionPayload(Map<String, Object> actionPayload) { this.actionPayload = actionPayload; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(int priorityOrder) { this.priorityOrder = priorityOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
