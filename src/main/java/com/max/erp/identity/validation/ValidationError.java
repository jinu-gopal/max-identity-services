package com.max.erp.identity.validation;

public record ValidationError(
        String fieldKey,
        String code,
        String message
) {
}
