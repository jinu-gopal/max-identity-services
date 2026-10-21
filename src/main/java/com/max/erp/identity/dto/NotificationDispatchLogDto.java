package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.Map;

public class NotificationDispatchLogDto {
    private String id;
    private String templateKey;
    private String templateName;
    private String channel;
    private String recipient;
    private String subject;
    private String bodyPreview;
    private String status; // DELIVERED, QUEUED, FAILED
    private Instant dispatchedAt;
    private long durationMs;
    private Map<String, Object> metadata;

    public NotificationDispatchLogDto() {}

    public NotificationDispatchLogDto(String id, String templateKey, String templateName,
                                      String channel, String recipient, String subject,
                                      String bodyPreview, String status, Instant dispatchedAt,
                                      long durationMs, Map<String, Object> metadata) {
        this.id = id;
        this.templateKey = templateKey;
        this.templateName = templateName;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.bodyPreview = bodyPreview;
        this.status = status;
        this.dispatchedAt = dispatchedAt;
        this.durationMs = durationMs;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyPreview() { return bodyPreview; }
    public void setBodyPreview(String bodyPreview) { this.bodyPreview = bodyPreview; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(Instant dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
