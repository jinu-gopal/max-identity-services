package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@Order(10)
public class RequiredFieldValidator implements RecordValidator {

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        for (BusinessObjectField field : context.fields()) {
            if (!Boolean.TRUE.equals(field.isRequired())) {
                continue;
            }

            Object value = values.get(field.getFieldKey());

            if (value == null) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "REQUIRED",
                        "Field is required"
                ));
                continue;
            }

            if (value instanceof String stringValue && stringValue.isBlank()) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "REQUIRED",
                        "Field is required"
                ));
                continue;
            }

            if (value instanceof Collection<?> collectionValue && collectionValue.isEmpty()) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "REQUIRED",
                        "Field is required"
                ));
            }
        }

        return errors;
    }
}
