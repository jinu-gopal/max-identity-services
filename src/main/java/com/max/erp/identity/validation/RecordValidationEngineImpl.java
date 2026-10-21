package com.max.erp.identity.validation;

import com.max.erp.identity.validation.validator.RecordValidator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecordValidationEngineImpl implements RecordValidationEngine {

    private final List<RecordValidator> validators;

    public RecordValidationEngineImpl(List<RecordValidator> validators) {
        this.validators = validators.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public ValidationResult validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();

        for (RecordValidator validator : validators) {
            errors.addAll(validator.validate(context));
        }

        if (errors.isEmpty()) {
            return ValidationResult.success();
        }

        return ValidationResult.failure(errors);
    }

    public List<RecordValidator> getValidators() {
        return validators;
    }

    private int orderOf(RecordValidator validator) {
        Integer order = OrderUtils.getOrder(validator.getClass());
        return order != null ? order : Integer.MAX_VALUE;
    }
}
