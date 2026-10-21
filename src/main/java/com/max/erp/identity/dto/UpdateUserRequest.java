package com.max.erp.identity.dto;

public record UpdateUserRequest(
        String email,
        String status,
        Boolean emailVerified,
        Boolean mfaEnabled
) {
}
