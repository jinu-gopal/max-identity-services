package com.max.erp.identity.service;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldOption;
import com.max.erp.identity.repository.BusinessObjectFieldOptionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class ImportFieldValueValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[-()\\s0-9]{7,20}$"
    );

    private final BusinessObjectFieldOptionRepository optionRepository;

    public ImportFieldValueValidator(
            BusinessObjectFieldOptionRepository optionRepository) {

        this.optionRepository = optionRepository;
    }

    public ValidationResult validate(
            BusinessObjectField field,
            String rawValue) {

        if (field == null) {
            return invalid(
                    "FIELD_NOT_FOUND",
                    "Target field was not found."
            );
        }

        String value = rawValue == null ? null : rawValue.trim();

        if (value == null || value.isEmpty()) {
            if (field.isRequired()) {
                return invalid(
                        "REQUIRED_FIELD",
                        "Required field '" + field.getLabel() + "' is missing."
                );
            }

            return valid(null);
        }

        String fieldType = normalizeFieldType(field.getFieldType());

        ValidationResult typeResult =
                validateType(field, value, fieldType);

        if (!typeResult.valid()) {
            return typeResult;
        }

        ValidationResult rangeResult =
                validateRange(field, value, fieldType);

        if (!rangeResult.valid()) {
            return rangeResult;
        }

        ValidationResult patternResult =
                validatePattern(field, value);

        if (!patternResult.valid()) {
            return patternResult;
        }

        return valid(normalizeValue(value, fieldType));
    }

    private ValidationResult validateType(
            BusinessObjectField field,
            String value,
            String fieldType) {

        return switch (fieldType) {
            case "TEXT",
                 "LONG_TEXT",
                 "ADDRESS",
                 "FILE",
                 "IMAGE",
                 "FORMULA",
                 "AUTO_NUMBER",
                 "REFERENCE",
                 "USER" -> valid(value);

            case "CHOICE" -> validateChoice(value, field);

            case "EMAIL" -> EMAIL_PATTERN.matcher(value).matches()
                    ? valid(value)
                    : invalid(
                            "INVALID_EMAIL",
                            "Invalid email value for field '" + field.getLabel() + "'."
                    );

            case "PHONE" -> PHONE_PATTERN.matcher(value).matches()
                    ? valid(value)
                    : invalid(
                            "INVALID_PHONE",
                            "Invalid phone value for field '" + field.getLabel() + "'."
                    );

            case "NUMBER" -> validateNumber(value, field);

            case "DECIMAL",
                 "CURRENCY",
                 "PERCENTAGE" -> validateDecimal(value, field);

            case "DATE" -> validateDate(value, field);

            case "DATETIME" -> validateDateTime(value, field);

            case "BOOLEAN" -> validateBoolean(value, field);

            case "MULTI_CHOICE" -> validateMultiChoice(value, field);

            default -> invalid(
                    "UNSUPPORTED_FIELD_TYPE",
                    "Unsupported field type '" + field.getFieldType()
                            + "' for field '" + field.getLabel() + "'."
            );
        };
    }

    private ValidationResult validateChoice(
            String value,
            BusinessObjectField field) {

        List<BusinessObjectFieldOption> options =
                optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(
                        field.getId()
                );

        for (BusinessObjectFieldOption option : options) {

            if (!option.isActive()) {
                continue;
            }

            if (value.equals(option.getOptionValue())
                    || value.equalsIgnoreCase(option.getOptionValue())
                    || value.equals(option.getOptionLabel())
                    || value.equalsIgnoreCase(option.getOptionLabel())) {

                return valid(option.getOptionValue());
            }
        }

        return invalid(
                "INVALID_CHOICE",
                "Value '" + value + "' is not a valid option for field '"
                        + field.getLabel() + "'."
        );
    }

    private ValidationResult validateMultiChoice(
            String value,
            BusinessObjectField field) {

        String[] parts = value.split(",");

        if (parts.length == 0) {
            return invalid(
                    "INVALID_MULTI_CHOICE",
                    "No valid options were supplied for field '"
                            + field.getLabel() + "'."
            );
        }

        List<BusinessObjectFieldOption> options =
                optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(
                        field.getId()
                );

        Map<String, String> canonicalValues = new HashMap<>();
        Set<String> seenValues = new HashSet<>();

        for (BusinessObjectFieldOption option : options) {

            if (!option.isActive()) {
                continue;
            }

            String canonical = option.getOptionValue();

            canonicalValues.put(
                    canonical.toLowerCase(Locale.ROOT),
                    canonical
            );

            canonicalValues.put(
                    option.getOptionLabel().toLowerCase(Locale.ROOT),
                    canonical
            );
        }

        StringBuilder normalized = new StringBuilder();

        for (String part : parts) {

            String item = part.trim();

            if (item.isEmpty()) {
                continue;
            }

            String canonical =
                    canonicalValues.get(item.toLowerCase(Locale.ROOT));

            if (canonical == null) {
                return invalid(
                        "INVALID_MULTI_CHOICE",
                        "Value '" + item
                                + "' is not a valid option for field '"
                                + field.getLabel() + "'."
                );
            }

            if (!seenValues.add(canonical.toLowerCase(Locale.ROOT))) {
                continue;
            }

            if (normalized.length() > 0) {
                normalized.append(",");
            }

            normalized.append(canonical);
        }

        if (normalized.length() == 0) {
            return invalid(
                    "INVALID_MULTI_CHOICE",
                    "No valid options were supplied for field '"
                            + field.getLabel() + "'."
            );
        }

        return valid(normalized.toString());
    }

    private ValidationResult validateNumber(
            String value,
            BusinessObjectField field) {

        try {
            BigDecimal number = new BigDecimal(value);

            if (number.stripTrailingZeros().scale() > 0) {
                return invalid(
                        "INVALID_NUMBER",
                        "Field '" + field.getLabel()
                                + "' requires a whole number."
                );
            }

            return valid(number.toBigInteger().toString());

        } catch (NumberFormatException ex) {
            return invalid(
                    "INVALID_NUMBER",
                    "Invalid numeric value for field '"
                            + field.getLabel() + "'."
            );
        }
    }

    private ValidationResult validateDecimal(
            String value,
            BusinessObjectField field) {

        try {
            BigDecimal number = new BigDecimal(value);

            return valid(
                    number.stripTrailingZeros().toPlainString()
            );

        } catch (NumberFormatException ex) {
            return invalid(
                    "INVALID_DECIMAL",
                    "Invalid decimal value for field '"
                            + field.getLabel() + "'."
            );
        }
    }

    private ValidationResult validateDate(
            String value,
            BusinessObjectField field) {

        try {
            LocalDate.parse(value);
            return valid(value);

        } catch (DateTimeParseException ex) {
            return invalid(
                    "INVALID_DATE",
                    "Invalid date for field '" + field.getLabel()
                            + "'. Expected ISO format yyyy-MM-dd."
            );
        }
    }

    private ValidationResult validateDateTime(
            String value,
            BusinessObjectField field) {

        try {
            OffsetDateTime.parse(value);
            return valid(value);

        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime.parse(value);
            return valid(value);

        } catch (DateTimeParseException ignored) {
        }

        try {
            Instant.parse(value);
            return valid(value);

        } catch (DateTimeParseException ignored) {
            return invalid(
                    "INVALID_DATETIME",
                    "Invalid date-time for field '"
                            + field.getLabel() + "'."
            );
        }
    }

    private ValidationResult validateBoolean(
            String value,
            BusinessObjectField field) {

        String normalized = value.toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "true", "false", "yes", "no", "1", "0" ->
                    valid(normalized);

            default -> invalid(
                    "INVALID_BOOLEAN",
                    "Invalid boolean value for field '"
                            + field.getLabel()
                            + "'. Use true/false, yes/no, or 1/0."
            );
        };
    }

    private ValidationResult validateRange(
            BusinessObjectField field,
            String value,
            String fieldType) {

        if (!isNumericType(fieldType)) {
            return valid(value);
        }

        BigDecimal number;

        try {
            number = new BigDecimal(value);

        } catch (NumberFormatException ex) {
            return valid(value);
        }

        if (field.getMinValue() != null
                && number.compareTo(field.getMinValue()) < 0) {

            return invalid(
                    "VALUE_BELOW_MINIMUM",
                    "Value for field '" + field.getLabel()
                            + "' is below the minimum allowed value "
                            + field.getMinValue() + "."
            );
        }

        if (field.getMaxValue() != null
                && number.compareTo(field.getMaxValue()) > 0) {

            return invalid(
                    "VALUE_ABOVE_MAXIMUM",
                    "Value for field '" + field.getLabel()
                            + "' exceeds the maximum allowed value "
                            + field.getMaxValue() + "."
            );
        }

        return valid(value);
    }

    private ValidationResult validatePattern(
            BusinessObjectField field,
            String value) {

        String pattern = field.getValidationPattern();

        if (pattern == null || pattern.isBlank()) {
            return valid(value);
        }

        try {
            if (!Pattern.compile(pattern).matcher(value).matches()) {
                return invalid(
                        "PATTERN_MISMATCH",
                        "Value for field '" + field.getLabel()
                                + "' does not match the configured validation pattern."
                );
            }

            return valid(value);

        } catch (PatternSyntaxException ex) {
            return invalid(
                    "INVALID_VALIDATION_PATTERN",
                    "The configured validation pattern for field '"
                            + field.getLabel() + "' is invalid."
            );
        }
    }

    private String normalizeValue(
            String value,
            String fieldType) {

        return switch (fieldType) {
            case "BOOLEAN" -> normalizeBoolean(value);

            case "NUMBER" -> new BigDecimal(value)
                    .toBigInteger()
                    .toString();

            case "DECIMAL",
                 "CURRENCY",
                 "PERCENTAGE" -> new BigDecimal(value)
                    .stripTrailingZeros()
                    .toPlainString();

            default -> value;
        };
    }

    private String normalizeBoolean(String value) {

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "yes", "1" -> "true";
            case "false", "no", "0" -> "false";
            default -> value;
        };
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

    private ValidationResult valid(String normalizedValue) {

        return new ValidationResult(
                true,
                null,
                null,
                normalizedValue
        );
    }

    private ValidationResult invalid(
            String errorCode,
            String errorMessage) {

        return new ValidationResult(
                false,
                errorCode,
                errorMessage,
                null
        );
    }

    public record ValidationResult(
            boolean valid,
            String errorCode,
            String errorMessage,
            String normalizedValue
    ) {
    }
}
