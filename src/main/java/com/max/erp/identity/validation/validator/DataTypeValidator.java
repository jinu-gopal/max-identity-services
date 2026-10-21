package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Order(20)
public class DataTypeValidator implements RecordValidator {

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        for (BusinessObjectField field : context.fields()) {
            Object value = values.get(field.getFieldKey());

            if (value == null) {
                continue;
            }

            if (!isValidType(field.getFieldType(), value)) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_TYPE",
                        "Value does not match field type " + field.getFieldType()
                ));
            }
        }

        return errors;
    }

    private boolean isValidType(String fieldType, Object value) {
        if (fieldType == null) {
            return false;
        }

        return switch (fieldType.toUpperCase()) {
            case "TEXT",
                 "LONG_TEXT",
                 "EMAIL",
                 "PHONE",
                 "ADDRESS" -> value instanceof String;

            case "NUMBER",
                 "DECIMAL",
                 "CURRENCY",
                 "PERCENTAGE" ->
                    value instanceof Number || value instanceof BigDecimal;

            case "BOOLEAN" -> value instanceof Boolean;

            case "DATE" ->
                    value instanceof LocalDate || value instanceof String;

            case "DATETIME" ->
                    value instanceof LocalDateTime
                            || value instanceof OffsetDateTime
                            || value instanceof Instant
                            || value instanceof String;

            case "CHOICE" -> value instanceof String;

            case "MULTI_CHOICE" ->
                    value instanceof Collection<?>;

            case "USER" ->
                    value instanceof UUID || value instanceof String;

            case "REFERENCE" ->
                    value instanceof UUID
                            || value instanceof String
                            || value instanceof Collection<?>;

            case "FILE",
                 "IMAGE" ->
                    value instanceof String
                            || value instanceof Map<?, ?>;

            default -> true;
        };
    }
}
