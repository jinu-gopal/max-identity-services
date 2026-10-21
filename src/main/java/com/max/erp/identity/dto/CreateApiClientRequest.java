package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateApiClientRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client name is required")
    private String clientName;

    private String grantType; // CLIENT_CREDENTIALS or AUTHORIZATION_CODE

    private List<String> scopes;

    private String redirectUri;

    public CreateApiClientRequest() {}

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
