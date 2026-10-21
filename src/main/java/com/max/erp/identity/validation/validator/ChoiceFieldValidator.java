package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldOption;
import com.max.erp.identity.repository.BusinessObjectFieldOptionRepository;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@Order(30)
public class ChoiceFieldValidator implements RecordValidator {

    private final BusinessObjectFieldOptionRepository optionRepository;

    public ChoiceFieldValidator(
            BusinessObjectFieldOptionRepository optionRepository
    ) {
        this.optionRepository = optionRepository;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        if (values == null || context.fields() == null) {
            return errors;
        }

        Map<UUID, Set<String>> activeOptionsCache = new HashMap<>();

        for (BusinessObjectField field : context.fields()) {
            String fieldType = normalizeFieldType(field.getFieldType());

            if (!"CHOICE".equals(fieldType) && !"MULTI_CHOICE".equals(fieldType)) {
                continue;
            }

            Object value = values.get(field.getFieldKey());

            if (value == null) {
                continue;
            }

            if ("CHOICE".equals(fieldType)) {
                validateSingleChoice(field, value, activeOptionsCache, errors);
            } else {
                validateMultiChoice(field, value, activeOptionsCache, errors);
            }
        }

        return errors;
    }

    private void validateSingleChoice(
            BusinessObjectField field,
            Object value,
            Map<UUID, Set<String>> activeOptionsCache,
            List<ValidationError> errors
    ) {
        if (!(value instanceof String rawChoice)) {
            return;
        }

        String choiceValue = rawChoice.trim();

        if (choiceValue.isEmpty()) {
            return;
        }

        Set<String> activeOptions = getActiveOptionValues(field, activeOptionsCache);

        if (!activeOptions.contains(choiceValue)) {
            errors.add(new ValidationError(
                    field.getFieldKey(),
                    "INVALID_CHOICE",
                    "Value '" + choiceValue + "' is not a valid active option for field " + field.getFieldKey()
            ));
        }
    }

    private void validateMultiChoice(
            BusinessObjectField field,
            Object value,
            Map<UUID, Set<String>> activeOptionsCache,
            List<ValidationError> errors
    ) {
        if (!(value instanceof Collection<?> items)) {
            return;
        }

        if (items.isEmpty()) {
            return;
        }

        Set<String> activeOptions = getActiveOptionValues(field, activeOptionsCache);

        for (Object rawItem : items) {
            if (!(rawItem instanceof String itemString)) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_MULTI_CHOICE",
                        "All values for multi-choice field " + field.getFieldKey() + " must be strings"
                ));
                return;
            }

            String candidate = itemString.trim();

            if (candidate.isEmpty() || !activeOptions.contains(candidate)) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_MULTI_CHOICE",
                        "Value '" + candidate + "' is not a valid active option for field " + field.getFieldKey()
                ));
                return;
            }
        }
    }

    private Set<String> getActiveOptionValues(
            BusinessObjectField field,
            Map<UUID, Set<String>> activeOptionsCache
    ) {
        UUID fieldId = field.getId();

        if (fieldId != null && activeOptionsCache.containsKey(fieldId)) {
            return activeOptionsCache.get(fieldId);
        }

        List<BusinessObjectFieldOption> options =
                optionRepository.findAllByFieldIdOrderByDisplayOrderAsc(fieldId);

        Set<String> activeValues = new LinkedHashSet<>();

        if (options != null) {
            for (BusinessObjectFieldOption option : options) {
                if (option != null
                        && option.isActive()
                        && option.getOptionValue() != null) {
                    String trimmed = option.getOptionValue().trim();
                    if (!trimmed.isEmpty()) {
                        activeValues.add(trimmed);
                    }
                }
            }
        }

        if (fieldId != null) {
            activeOptionsCache.put(fieldId, activeValues);
        }

        return activeValues;
    }

    private String normalizeFieldType(String fieldType) {
        return fieldType == null
                ? ""
                : fieldType.trim().toUpperCase(Locale.ROOT);
    }
}
