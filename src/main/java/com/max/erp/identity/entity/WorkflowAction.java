package com.max.erp.identity.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.max.erp.identity.enums.WorkflowActionType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "configuration_workflow_actions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WorkflowAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "instance_id", nullable = false)
    private UUID instanceId;

    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowActionType action;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comments;

    @Column(name = "action_timestamp", nullable = false, updatable = false)
    private Instant actionTimestamp;

    @PrePersist
    protected void onCreate() {
        if (actionTimestamp == null) {
            actionTimestamp = Instant.now();
        }
    }

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

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
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
