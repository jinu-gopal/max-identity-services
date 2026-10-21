package com.max.erp.identity.validation.validator;

import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;

import java.util.List;

public interface RecordValidator {

    List<ValidationError> validate(ValidationContext context);
}
