package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.List;

public class NotificationTemplateDto {
    private String id;
    private String templateKey;
    private String name;
    private String channel; // EMAIL, IN_APP, SLACK, WEBHOOK
    private String triggerEvent; // WORKFLOW_PENDING_APPROVAL, WORKFLOW_APPROVED, WORKFLOW_REJECTED, RECORD_CREATED, RECORD_STATUS_CHANGED, HIGH_VALUE_TRANSACTION
    private String subject;
    private String body;
    private String recipientExpression;
    private List<String> availableMergeTags;
    private String status; // ACTIVE, INACTIVE
    private Instant updatedAt;

    public NotificationTemplateDto() {}

    public NotificationTemplateDto(String id, String templateKey, String name, String channel,
                                   String triggerEvent, String subject, String body,
                                   String recipientExpression, List<String> availableMergeTags,
                                   String status, Instant updatedAt) {
        this.id = id;
        this.templateKey = templateKey;
        this.name = name;
        this.channel = channel;
        this.triggerEvent = triggerEvent;
        this.subject = subject;
        this.body = body;
        this.recipientExpression = recipientExpression;
        this.availableMergeTags = availableMergeTags;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getRecipientExpression() { return recipientExpression; }
    public void setRecipientExpression(String recipientExpression) { this.recipientExpression = recipientExpression; }

    public List<String> getAvailableMergeTags() { return availableMergeTags; }
    public void setAvailableMergeTags(List<String> availableMergeTags) { this.availableMergeTags = availableMergeTags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
