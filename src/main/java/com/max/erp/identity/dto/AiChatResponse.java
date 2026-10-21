package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AiChatResponse {
    private String reply;
    private String actionType; // PROPOSE_SCHEMA, PROPOSE_WORKFLOW, DATA_INSIGHT, GENERAL_ASSISTANCE
    private AiGeneratedSchemaDto proposedSchema;
    private AiGeneratedWorkflowDto proposedWorkflow;
    private List<String> suggestedQueries = new ArrayList<>();
    private Instant timestamp;

    public AiChatResponse() {
        this.timestamp = Instant.now();
    }

    public AiChatResponse(String reply, String actionType, AiGeneratedSchemaDto proposedSchema,
                          AiGeneratedWorkflowDto proposedWorkflow, List<String> suggestedQueries) {
        this.reply = reply;
        this.actionType = actionType;
        this.proposedSchema = proposedSchema;
        this.proposedWorkflow = proposedWorkflow;
        this.suggestedQueries = suggestedQueries != null ? suggestedQueries : new ArrayList<>();
        this.timestamp = Instant.now();
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public AiGeneratedSchemaDto getProposedSchema() { return proposedSchema; }
    public void setProposedSchema(AiGeneratedSchemaDto proposedSchema) { this.proposedSchema = proposedSchema; }

    public AiGeneratedWorkflowDto getProposedWorkflow() { return proposedWorkflow; }
    public void setProposedWorkflow(AiGeneratedWorkflowDto proposedWorkflow) { this.proposedWorkflow = proposedWorkflow; }

    public List<String> getSuggestedQueries() { return suggestedQueries; }
    public void setSuggestedQueries(List<String> suggestedQueries) { this.suggestedQueries = suggestedQueries; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
