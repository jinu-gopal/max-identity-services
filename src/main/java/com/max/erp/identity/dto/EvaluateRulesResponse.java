package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluateRulesResponse {
    private boolean valid;
    private List<String> validationErrors = new ArrayList<>();
    private Map<String, Boolean> fieldVisibility = new HashMap<>();
    private Map<String, Boolean> fieldLocked = new HashMap<>();
    private Map<String, Object> computedFields = new HashMap<>();
    private List<String> appliedRuleKeys = new ArrayList<>();
    private List<String> evaluationLogs = new ArrayList<>();

    public EvaluateRulesResponse() {
        this.valid = true;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public List<String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }

    public Map<String, Boolean> getFieldVisibility() { return fieldVisibility; }
    public void setFieldVisibility(Map<String, Boolean> fieldVisibility) { this.fieldVisibility = fieldVisibility; }

    public Map<String, Boolean> getFieldLocked() { return fieldLocked; }
    public void setFieldLocked(Map<String, Boolean> fieldLocked) { this.fieldLocked = fieldLocked; }

    public Map<String, Object> getComputedFields() { return computedFields; }
    public void setComputedFields(Map<String, Object> computedFields) { this.computedFields = computedFields; }

    public List<String> getAppliedRuleKeys() { return appliedRuleKeys; }
    public void setAppliedRuleKeys(List<String> appliedRuleKeys) { this.appliedRuleKeys = appliedRuleKeys; }

    public List<String> getEvaluationLogs() { return evaluationLogs; }
    public void setEvaluationLogs(List<String> evaluationLogs) { this.evaluationLogs = evaluationLogs; }
}
