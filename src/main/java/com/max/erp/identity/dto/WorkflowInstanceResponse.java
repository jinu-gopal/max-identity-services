package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowInstanceStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorkflowInstanceResponse {

    private UUID id;
    private UUID tenantId;
    private UUID workflowId;
    private String workflowName;
    private String workflowKey;
    private UUID businessObjectId;
    private String businessObjectName;
    private String businessObjectKey;
    private UUID recordId;
    private UUID currentStepId;
    private String currentStepName;
    private Integer currentStepOrder;
    private Integer totalSteps;
    private UUID approverRoleId;
    private String approverRoleName;
    private WorkflowInstanceStatus status;
    private UUID initiatedBy;
    private String initiatorUsername;
    private Instant initiatedAt;
    private Instant completedAt;
    private Map<String, Object> contextData;
    private List<WorkflowActionResponse> actions = new ArrayList<>();
    private List<WorkflowStepResponse> steps = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(UUID workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(String workflowKey) {
        this.workflowKey = workflowKey;
    }

    public UUID getBusinessObjectId() {
        return businessObjectId;
    }

    public void setBusinessObjectId(UUID businessObjectId) {
        this.businessObjectId = businessObjectId;
    }

    public String getBusinessObjectName() {
        return businessObjectName;
    }

    public void setBusinessObjectName(String businessObjectName) {
        this.businessObjectName = businessObjectName;
    }

    public String getBusinessObjectKey() {
        return businessObjectKey;
    }

    public void setBusinessObjectKey(String businessObjectKey) {
        this.businessObjectKey = businessObjectKey;
    }

    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
    }

    public UUID getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(UUID currentStepId) {
        this.currentStepId = currentStepId;
    }

    public String getCurrentStepName() {
        return currentStepName;
    }

    public void setCurrentStepName(String currentStepName) {
        this.currentStepName = currentStepName;
    }

    public Integer getCurrentStepOrder() {
        return currentStepOrder;
    }

    public void setCurrentStepOrder(Integer currentStepOrder) {
        this.currentStepOrder = currentStepOrder;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public UUID getApproverRoleId() {
        return approverRoleId;
    }

    public void setApproverRoleId(UUID approverRoleId) {
        this.approverRoleId = approverRoleId;
    }

    public String getApproverRoleName() {
        return approverRoleName;
    }

    public void setApproverRoleName(String approverRoleName) {
        this.approverRoleName = approverRoleName;
    }

    public WorkflowInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowInstanceStatus status) {
        this.status = status;
    }

    public UUID getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(UUID initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getInitiatorUsername() {
        return initiatorUsername;
    }

    public void setInitiatorUsername(String initiatorUsername) {
        this.initiatorUsername = initiatorUsername;
    }

    public Instant getInitiatedAt() {
        return initiatedAt;
    }

    public void setInitiatedAt(Instant initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public void setContextData(Map<String, Object> contextData) {
        this.contextData = contextData;
    }

    public List<WorkflowActionResponse> getActions() {
        return actions;
    }

    public void setActions(List<WorkflowActionResponse> actions) {
        this.actions = actions;
    }

    public List<WorkflowStepResponse> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStepResponse> steps) {
        this.steps = steps;
    }
}
