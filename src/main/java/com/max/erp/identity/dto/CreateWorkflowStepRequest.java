package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowStepType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateWorkflowStepRequest {

    @NotNull
    private Integer stepOrder;

    @NotBlank
    private String name;

    private WorkflowStepType stepType = WorkflowStepType.APPROVAL;

    private UUID approverRoleId;

    private UUID approverUserId;

    private String conditionExpression;

    private String statusOnApproval = "APPROVED";

    private String statusOnRejection = "REJECTED";

    private Integer timeoutHours;

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

    public UUID getApproverUserId() {
        return approverUserId;
    }

    public void setApproverUserId(UUID approverUserId) {
        this.approverUserId = approverUserId;
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
}
