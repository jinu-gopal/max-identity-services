package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PendingApprovalResponse {

    private UUID instanceId;
    private UUID tenantId;
    private UUID workflowId;
    private String workflowName;
    private UUID businessObjectId;
    private String businessObjectName;
    private String businessObjectKey;
    private UUID recordId;
    private UUID stepId;
    private String stepName;
    private Integer stepOrder;
    private UUID approverRoleId;
    private String approverRoleName;
    private UUID approverUserId;
    private UUID initiatedBy;
    private String initiatorUsername;
    private Instant initiatedAt;
    private Map<String, Object> contextData;

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
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

    public UUID getStepId() {
        return stepId;
    }

    public void setStepId(UUID stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
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

    public UUID getApproverUserId() {
        return approverUserId;
    }

    public void setApproverUserId(UUID approverUserId) {
        this.approverUserId = approverUserId;
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

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public void setContextData(Map<String, Object> contextData) {
        this.contextData = contextData;
    }
}
