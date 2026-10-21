package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateNotificationTemplateRequest {

    @NotBlank(message = "Template key is required")
    private String templateKey;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Channel is required")
    private String channel; // EMAIL, IN_APP, SLACK, WEBHOOK

    @NotBlank(message = "Trigger event is required")
    private String triggerEvent;

    private String subject;

    @NotBlank(message = "Template body is required")
    private String body;

    private String recipientExpression;

    private List<String> availableMergeTags;

    private String status = "ACTIVE";

    public CreateNotificationTemplateRequest() {}

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
}
