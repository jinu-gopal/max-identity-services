package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.Map;

public class ConnectorDto {
    private String id;
    private String name;
    private String category; // CRM, ERP, PAYMENTS, COMMUNICATION, HR
    private String description;
    private String status; // CONNECTED, CONFIGURED, AVAILABLE
    private Instant lastSyncAt;
    private Map<String, Object> config;

    public ConnectorDto() {}

    public ConnectorDto(String id, String name, String category, String description,
                        String status, Instant lastSyncAt, Map<String, Object> config) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.status = status;
        this.lastSyncAt = lastSyncAt;
        this.config = config;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
}
