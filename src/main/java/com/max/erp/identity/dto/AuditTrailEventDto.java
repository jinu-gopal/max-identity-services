package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.Map;

public class AuditTrailEventDto {

    private String id;
    private String category; // WORKFLOW, AUTH, RBAC, IMPORT, DATA
    private String eventType;
    private String severity; // INFO, WARNING, CRITICAL
    private String actorUsername;
    private String targetResource;
    private String description;
    private Instant timestamp;
    private Map<String, Object> metadata;

    public AuditTrailEventDto() {}

    public AuditTrailEventDto(
            String id,
            String category,
            String eventType,
            String severity,
            String actorUsername,
            String targetResource,
            String description,
            Instant timestamp,
            Map<String, Object> metadata
    ) {
        this.id = id;
        this.category = category;
        this.eventType = eventType;
        this.severity = severity;
        this.actorUsername = actorUsername;
        this.targetResource = targetResource;
        this.description = description;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(String targetResource) {
        this.targetResource = targetResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
