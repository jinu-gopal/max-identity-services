package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowStepType;
import java.time.Instant;
import java.util.UUID;

public class WorkflowStepResponse {

    private UUID id;
    private UUID workflowId;
    private Integer stepOrder;
    private String name;
    private WorkflowStepType stepType;
    private UUID approverRoleId;
    private String approverRoleName;
    private UUID approverUserId;
    private String approverUsername;
    private String conditionExpression;
    private String statusOnApproval;
    private String statusOnRejection;
    private Integer timeoutHours;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(UUID workflowId) {
        this.workflowId = workflowId;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkflowStepType getStepType() {
        return stepType;
    }

    public void setStepType(WorkflowStepType stepType) {
        this.stepType = stepType;
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

    public String getApproverUsername() {
        return approverUsername;
    }

    public void setApproverUsername(String approverUsername) {
        this.approverUsername = approverUsername;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getStatusOnApproval() {
        return statusOnApproval;
    }

    public void setStatusOnApproval(String statusOnApproval) {
        this.statusOnApproval = statusOnApproval;
    }

    public String getStatusOnRejection() {
        return statusOnRejection;
    }

    public void setStatusOnRejection(String statusOnRejection) {
        this.statusOnRejection = statusOnRejection;
    }

    public Integer getTimeoutHours() {
        return timeoutHours;
    }

    public void setTimeoutHours(Integer timeoutHours) {
        this.timeoutHours = timeoutHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
