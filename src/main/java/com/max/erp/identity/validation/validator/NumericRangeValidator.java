package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Order(40)
public class NumericRangeValidator implements RecordValidator {

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        if (values == null || context.fields() == null) {
            return errors;
        }

        for (BusinessObjectField field : context.fields()) {
            String fieldType = normalizeFieldType(field.getFieldType());

            if (!isNumericType(fieldType)) {
                continue;
            }

            if (field.getMinValue() == null && field.getMaxValue() == null) {
                continue;
            }

            Object value = values.get(field.getFieldKey());

            if (value == null) {
                continue;
            }

            BigDecimal numericValue = toBigDecimal(value);

            if (numericValue == null) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_TYPE",
                        "Value does not match field type " + field.getFieldType()
                ));
                continue;
            }

            if (field.getMinValue() != null
                    && numericValue.compareTo(field.getMinValue()) < 0) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "VALUE_BELOW_MINIMUM",
                        "Value for field " + field.getFieldKey()
                                + " is below the minimum allowed value "
                                + field.getMinValue()
                ));
                continue;
            }

            if (field.getMaxValue() != null
                    && numericValue.compareTo(field.getMaxValue()) > 0) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "VALUE_ABOVE_MAXIMUM",
                        "Value for field " + field.getFieldKey()
                                + " exceeds the maximum allowed value "
                                + field.getMaxValue()
                ));
            }
        }

        return errors;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof BigInteger bigInteger) {
            return new BigDecimal(bigInteger);
        }

        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }

        if (value instanceof Float floatValue) {
            if (!Float.isFinite(floatValue)) {
                return null;
            }
            return new BigDecimal(Float.toString(floatValue));
        }

        if (value instanceof Double doubleValue) {
            if (!Double.isFinite(doubleValue)) {
                return null;
            }
            return BigDecimal.valueOf(doubleValue);
        }

        if (value instanceof Number numberValue) {
            try {
                return new BigDecimal(numberValue.toString());
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        return null;
    }

    private boolean isNumericType(String fieldType) {
        return switch (fieldType) {
            case "NUMBER",
                 "DECIMAL",
                 "CURRENCY",
                 "PERCENTAGE" -> true;
            default -> false;
        };
    }

    private String normalizeFieldType(String fieldType) {
        return fieldType == null
                ? ""
                : fieldType.trim().toUpperCase(Locale.ROOT);
    }
}
