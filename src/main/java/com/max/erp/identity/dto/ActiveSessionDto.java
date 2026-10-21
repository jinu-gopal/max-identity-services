package com.max.erp.identity.dto;

import java.time.Instant;

public class ActiveSessionDto {

    private String id;
    private String principalName;
    private String clientId;
    private String grantType;
    private Instant issuedAt;
    private Instant expiresAt;
    private String status; // ACTIVE, EXPIRING_SOON, EXPIRED
    private long secondsRemaining;

    public ActiveSessionDto() {}

    public ActiveSessionDto(
            String id,
            String principalName,
            String clientId,
            String grantType,
            Instant issuedAt,
            Instant expiresAt,
            String status,
            long secondsRemaining
    ) {
        this.id = id;
        this.principalName = principalName;
        this.clientId = clientId;
        this.grantType = grantType;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.secondsRemaining = secondsRemaining;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSecondsRemaining() {
        return secondsRemaining;
    }

    public void setSecondsRemaining(long secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }
}
