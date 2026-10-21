package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.List;

public class ApiClientDto {
    private String id;
    private String clientId;
    private String clientName;
    private String clientSecret; // Only populated on creation or masked
    private List<String> grantTypes;
    private List<String> scopes;
    private String redirectUris;
    private Instant issuedAt;
    private String status;

    public ApiClientDto() {}

    public ApiClientDto(String id, String clientId, String clientName, String clientSecret,
                        List<String> grantTypes, List<String> scopes, String redirectUris,
                        Instant issuedAt, String status) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientSecret = clientSecret;
        this.grantTypes = grantTypes;
        this.scopes = scopes;
        this.redirectUris = redirectUris;
        this.issuedAt = issuedAt;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public List<String> getGrantTypes() { return grantTypes; }
    public void setGrantTypes(List<String> grantTypes) { this.grantTypes = grantTypes; }

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    public String getRedirectUris() { return redirectUris; }
    public void setRedirectUris(String redirectUris) { this.redirectUris = redirectUris; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
