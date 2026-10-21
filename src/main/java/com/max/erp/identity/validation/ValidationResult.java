package com.max.erp.identity.validation;

import java.util.List;

public record ValidationResult(
        boolean valid,
        List<ValidationError> errors
) {

    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(false, List.copyOf(errors));
    }
}
