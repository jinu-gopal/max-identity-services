package com.max.erp.identity.authorization;

public enum AuthorizationDecision {

    ALLOW,

    TENANT_ACCESS_DENIED,

    MODULE_NOT_ENTITLED,

    ORGANIZATION_SCOPE_DENIED,

    ROLE_NOT_APPLICABLE,

    PERMISSION_DENIED
}