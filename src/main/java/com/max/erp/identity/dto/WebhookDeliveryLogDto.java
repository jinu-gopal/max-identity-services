package com.max.erp.identity.dto;

import java.time.Instant;

public class WebhookDeliveryLogDto {
    private String id;
    private String webhookId;
    private String webhookName;
    private String eventType;
    private String targetUrl;
    private int statusCode;
    private long latencyMs;
    private Instant deliveredAt;
    private boolean success;
    private String responseBody;

    public WebhookDeliveryLogDto() {}

    public WebhookDeliveryLogDto(String id, String webhookId, String webhookName, String eventType,
                                 String targetUrl, int statusCode, long latencyMs,
                                 Instant deliveredAt, boolean success, String responseBody) {
        this.id = id;
        this.webhookId = webhookId;
        this.webhookName = webhookName;
        this.eventType = eventType;
        this.targetUrl = targetUrl;
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.deliveredAt = deliveredAt;
        this.success = success;
        this.responseBody = responseBody;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWebhookId() { return webhookId; }
    public void setWebhookId(String webhookId) { this.webhookId = webhookId; }

    public String getWebhookName() { return webhookName; }
    public void setWebhookName(String webhookName) { this.webhookName = webhookName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
}
