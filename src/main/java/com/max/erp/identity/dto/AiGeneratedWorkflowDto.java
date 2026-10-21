package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.List;

public class AiGeneratedWorkflowDto {
    private String workflowKey;
    private String name;
    private String description;
    private String businessObjectKey;
    private List<StepDraftDto> steps = new ArrayList<>();

    public AiGeneratedWorkflowDto() {}

    public AiGeneratedWorkflowDto(String workflowKey, String name, String description, String businessObjectKey, List<StepDraftDto> steps) {
        this.workflowKey = workflowKey;
        this.name = name;
        this.description = description;
        this.businessObjectKey = businessObjectKey;
        this.steps = steps != null ? steps : new ArrayList<>();
    }

    public String getWorkflowKey() { return workflowKey; }
    public void setWorkflowKey(String workflowKey) { this.workflowKey = workflowKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBusinessObjectKey() { return businessObjectKey; }
    public void setBusinessObjectKey(String businessObjectKey) { this.businessObjectKey = businessObjectKey; }

    public List<StepDraftDto> getSteps() { return steps; }
    public void setSteps(List<StepDraftDto> steps) { this.steps = steps; }

    public static class StepDraftDto {
        private int stepOrder;
        private String name;
        private String stepType; // APPROVAL, REVIEW, NOTIFICATION
        private String approverRoleKey;
        private String approverRoleName;
        private String conditionExpression;

        public StepDraftDto() {}

        public StepDraftDto(int stepOrder, String name, String stepType, String approverRoleKey, String approverRoleName, String conditionExpression) {
            this.stepOrder = stepOrder;
            this.name = name;
            this.stepType = stepType;
            this.approverRoleKey = approverRoleKey;
            this.approverRoleName = approverRoleName;
            this.conditionExpression = conditionExpression;
        }

        public int getStepOrder() { return stepOrder; }
        public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStepType() { return stepType; }
        public void setStepType(String stepType) { this.stepType = stepType; }

        public String getApproverRoleKey() { return approverRoleKey; }
        public void setApproverRoleKey(String approverRoleKey) { this.approverRoleKey = approverRoleKey; }

        public String getApproverRoleName() { return approverRoleName; }
        public void setApproverRoleName(String approverRoleName) { this.approverRoleName = approverRoleName; }

        public String getConditionExpression() { return conditionExpression; }
        public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    }
}
