package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class CreateBusinessRuleRequest {

    @NotBlank(message = "Rule key is required")
    private String ruleKey;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String businessObjectId;
    private String businessObjectName;

    @NotBlank(message = "Rule type is required")
    private String ruleType; // VALIDATION, VISIBILITY, COMPUTATION, LOCKING

    private String triggerEvent = "ON_CHANGE"; // ON_CHANGE, ON_SUBMIT, ON_LOAD

    private String conditionExpression;

    private Map<String, Object> actionPayload;

    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    private int priorityOrder = 1;

    public CreateBusinessRuleRequest() {}

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
}
