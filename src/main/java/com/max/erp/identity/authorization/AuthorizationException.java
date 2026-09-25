package com.max.erp.identity.authorization;

public class AuthorizationException extends RuntimeException {

    private final String code;

    public AuthorizationException(
            String code,
            String message) {

        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}