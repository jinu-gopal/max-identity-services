package com.max.erp.identity.dto;

import com.max.erp.identity.enums.WorkflowActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class WorkflowDecisionRequest {

    @NotNull
    private WorkflowActionType action;

    @NotBlank(message = "Decision reasoning comment is mandatory")
    private String comments;

    private UUID delegatedToUserId;

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

    public UUID getDelegatedToUserId() {
        return delegatedToUserId;
    }

    public void setDelegatedToUserId(UUID delegatedToUserId) {
        this.delegatedToUserId = delegatedToUserId;
    }
}
