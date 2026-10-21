package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;

public class AiChatRequest {

    @NotBlank(message = "Message prompt cannot be empty")
    private String message;

    private String context; // SCHEMA_DESIGN, WORKFLOW_DESIGN, OPERATIONS, GENERAL

    private String conversationId;

    public AiChatRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
