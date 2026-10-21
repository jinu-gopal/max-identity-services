package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateWebhookRequest {

    @NotBlank(message = "Webhook name is required")
    private String name;

    @NotBlank(message = "Target URL is required")
    private String targetUrl;

    @NotEmpty(message = "At least one subscription event is required")
    private List<String> events;

    private String secret;

    public CreateWebhookRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
