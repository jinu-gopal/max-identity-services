package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowStatus;
import com.max.erp.identity.enums.WorkflowTriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateWorkflowRequest {

    @NotNull
    private UUID businessObjectId;

    @NotBlank
    private String workflowKey;

    @NotBlank
    private String name;

    private String description;

    private WorkflowTriggerType triggerType = WorkflowTriggerType.MANUAL_SUBMISSION;

    private WorkflowStatus status = WorkflowStatus.ACTIVE;

    private List<CreateWorkflowStepRequest> steps = new ArrayList<>();

    public UUID getBusinessObjectId() {
        return businessObjectId;
    }

    public void setBusinessObjectId(UUID businessObjectId) {
        this.businessObjectId = businessObjectId;
    }

    public String getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(String workflowKey) {
        this.workflowKey = workflowKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkflowTriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(WorkflowTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public List<CreateWorkflowStepRequest> getSteps() {
        return steps;
    }

    public void setSteps(List<CreateWorkflowStepRequest> steps) {
        this.steps = steps;
    }
}
