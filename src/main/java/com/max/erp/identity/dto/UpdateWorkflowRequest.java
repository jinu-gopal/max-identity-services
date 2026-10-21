package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowStatus;
import com.max.erp.identity.enums.WorkflowTriggerType;

public class UpdateWorkflowRequest {

    private String name;
    private String description;
    private WorkflowTriggerType triggerType;
    private WorkflowStatus status;

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
}
