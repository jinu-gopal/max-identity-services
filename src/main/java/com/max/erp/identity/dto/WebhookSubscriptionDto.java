package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.List;

public class WebhookSubscriptionDto {
    private String id;
    private String name;
    private String targetUrl;
    private List<String> events;
    private String secret;
    private String status; // ACTIVE, PAUSED, FAILING
    private Instant createdAt;
    private Integer lastDeliveryStatusCode;
    private Instant lastDeliveryAt;

    public WebhookSubscriptionDto() {}

    public WebhookSubscriptionDto(String id, String name, String targetUrl, List<String> events,
                                  String secret, String status, Instant createdAt,
                                  Integer lastDeliveryStatusCode, Instant lastDeliveryAt) {
        this.id = id;
        this.name = name;
        this.targetUrl = targetUrl;
        this.events = events;
        this.secret = secret;
        this.status = status;
        this.createdAt = createdAt;
        this.lastDeliveryStatusCode = lastDeliveryStatusCode;
        this.lastDeliveryAt = lastDeliveryAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Integer getLastDeliveryStatusCode() { return lastDeliveryStatusCode; }
    public void setLastDeliveryStatusCode(Integer lastDeliveryStatusCode) { this.lastDeliveryStatusCode = lastDeliveryStatusCode; }

    public Instant getLastDeliveryAt() { return lastDeliveryAt; }
    public void setLastDeliveryAt(Instant lastDeliveryAt) { this.lastDeliveryAt = lastDeliveryAt; }
}
