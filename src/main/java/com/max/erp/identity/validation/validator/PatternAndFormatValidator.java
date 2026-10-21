package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
@Order(50)
public class PatternAndFormatValidator implements RecordValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[-()\\s0-9]{7,20}$"
    );

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        if (values == null || context.fields() == null) {
            return errors;
        }

        for (BusinessObjectField field : context.fields()) {
            Object value = values.get(field.getFieldKey());

            if (value == null) {
                continue;
            }

            if (value instanceof String stringValue && stringValue.isBlank()) {
                continue;
            }

            String fieldType = normalizeFieldType(field.getFieldType());

            if (!validateFieldFormat(field, fieldType, value, errors)) {
                continue;
            }

            if (isPatternApplicable(fieldType) && value instanceof String stringValue) {
                validateConfiguredPattern(field, stringValue, errors);
            }
        }

        return errors;
    }

    private boolean validateFieldFormat(
            BusinessObjectField field,
            String fieldType,
            Object value,
            List<ValidationError> errors
    ) {
        return switch (fieldType) {
            case "EMAIL" -> validateEmail(field, value, errors);
            case "PHONE" -> validatePhone(field, value, errors);
            case "DATE" -> validateDate(field, value, errors);
            case "DATETIME" -> validateDateTime(field, value, errors);
            default -> true;
        };
    }

    private boolean validateEmail(
            BusinessObjectField field,
            Object value,
            List<ValidationError> errors
    ) {
        if (!(value instanceof String rawValue)) {
            return false;
        }

        String trimmed = rawValue.trim();

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            errors.add(new ValidationError(
                    field.getFieldKey(),
                    "INVALID_EMAIL",
                    "Invalid email value for field " + field.getFieldKey()
            ));
            return false;
        }

        return true;
    }

    private boolean validatePhone(
            BusinessObjectField field,
            Object value,
            List<ValidationError> errors
    ) {
        if (!(value instanceof String rawValue)) {
            return false;
        }

        String trimmed = rawValue.trim();

        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            errors.add(new ValidationError(
                    field.getFieldKey(),
                    "INVALID_PHONE",
                    "Invalid phone value for field " + field.getFieldKey()
            ));
            return false;
        }

        return true;
    }

    private boolean validateDate(
            BusinessObjectField field,
            Object value,
            List<ValidationError> errors
    ) {
        if (value instanceof LocalDate) {
            return true;
        }

        if (!(value instanceof String rawValue)) {
            return false;
        }

        String trimmed = rawValue.trim();

        try {
            LocalDate.parse(trimmed);
            return true;
        } catch (DateTimeParseException ex) {
            errors.add(new ValidationError(
                    field.getFieldKey(),
                    "INVALID_DATE",
                    "Invalid date for field " + field.getFieldKey()
                            + ". Expected ISO format yyyy-MM-dd"
            ));
            return false;
        }
    }

    private boolean validateDateTime(
            BusinessObjectField field,
            Object value,
            List<ValidationError> errors
    ) {
        if (value instanceof LocalDateTime
                || value instanceof OffsetDateTime
                || value instanceof Instant) {
            return true;
        }

        if (!(value instanceof String rawValue)) {
            return false;
        }

        String trimmed = rawValue.trim();

        if (isValidIsoDateTime(trimmed)) {
            return true;
        }

        errors.add(new ValidationError(
                field.getFieldKey(),
                "INVALID_DATETIME",
                "Invalid date-time for field " + field.getFieldKey()
        ));
        return false;
    }

    private boolean isValidIsoDateTime(String value) {
        try {
            OffsetDateTime.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
        }

        try {
            Instant.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    private void validateConfiguredPattern(
            BusinessObjectField field,
            String value,
            List<ValidationError> errors
    ) {
        String pattern = field.getValidationPattern();

        if (pattern == null || pattern.isBlank()) {
            return;
        }

        try {
            if (!Pattern.compile(pattern).matcher(value).matches()) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "PATTERN_MISMATCH",
                        "Value for field " + field.getFieldKey()
                                + " does not match the configured validation pattern"
                ));
            }
        } catch (PatternSyntaxException ex) {
            errors.add(new ValidationError(
                    field.getFieldKey(),
                    "INVALID_VALIDATION_PATTERN",
                    "The configured validation pattern for field " + field.getFieldKey()
                            + " is invalid"
            ));
        }
    }

    private boolean isPatternApplicable(String fieldType) {
        return switch (fieldType) {
            case "TEXT",
                 "LONG_TEXT",
                 "EMAIL",
                 "PHONE",
                 "ADDRESS",
                 "DATE",
                 "DATETIME" -> true;
            default -> false;
        };
    }

    private String normalizeFieldType(String fieldType) {
        return fieldType == null
                ? ""
                : fieldType.trim().toUpperCase(Locale.ROOT);
    }
}
