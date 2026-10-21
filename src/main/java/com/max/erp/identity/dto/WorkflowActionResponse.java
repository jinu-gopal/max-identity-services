package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowActionType;
import java.time.Instant;
import java.util.UUID;

public class WorkflowActionResponse {

    private UUID id;
    private UUID instanceId;
    private UUID stepId;
    private String stepName;
    private Integer stepOrder;
    private UUID actorUserId;
    private String actorUsername;
    private WorkflowActionType action;
    private String comments;
    private Instant actionTimestamp;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
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

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public WorkflowActionType getAction() {
        return action;
    }

    public void setAction(WorkflowActionType action) {
        this.action = action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Instant getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(Instant actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }
}
