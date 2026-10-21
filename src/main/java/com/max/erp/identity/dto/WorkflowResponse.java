package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowStatus;
import com.max.erp.identity.enums.WorkflowTriggerType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkflowResponse {

    private UUID id;
    private UUID tenantId;
    private UUID businessObjectId;
    private String businessObjectName;
    private String businessObjectKey;
    private String workflowKey;
    private String name;
    private String description;
    private WorkflowTriggerType triggerType;
    private WorkflowStatus status;
    private List<WorkflowStepResponse> steps = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

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

    public List<WorkflowStepResponse> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStepResponse> steps) {
        this.steps = steps;
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
