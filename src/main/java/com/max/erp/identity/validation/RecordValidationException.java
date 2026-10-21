package com.max.erp.identity.validation;

import java.util.List;

public class RecordValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public RecordValidationException(List<ValidationError> errors) {
        super("One or more fields are invalid");
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
