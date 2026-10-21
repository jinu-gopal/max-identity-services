package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectFieldReference;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.repository.BusinessObjectFieldReferenceRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Order(60)
public class ReferenceFieldValidator implements RecordValidator {

    private final BusinessObjectFieldReferenceRepository referenceRepository;
    private final BusinessObjectRecordRepository recordRepository;

    public ReferenceFieldValidator(
            BusinessObjectFieldReferenceRepository referenceRepository,
            BusinessObjectRecordRepository recordRepository
    ) {
        this.referenceRepository = referenceRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        if (values == null || context.fields() == null) {
            return errors;
        }

        Map<BusinessObjectField, ParsedFieldReference> pendingValidations =
                new LinkedHashMap<>();
        Set<UUID> allReferencedRecordIds = new LinkedHashSet<>();
        Map<UUID, Optional<BusinessObjectFieldReference>> referenceConfigCache =
                new HashMap<>();

        for (BusinessObjectField field : context.fields()) {
            String fieldType = normalizeFieldType(field.getFieldType());

            if (!"REFERENCE".equals(fieldType)) {
                continue;
            }

            Object value = values.get(field.getFieldKey());

            if (value == null) {
                continue;
            }

            if (value instanceof String stringValue && stringValue.isBlank()) {
                continue;
            }

            if (value instanceof Collection<?> collection && collection.isEmpty()) {
                continue;
            }

            BusinessObjectFieldReference referenceConfig =
                    findReferenceConfig(field, referenceConfigCache);

            if (referenceConfig == null
                    || referenceConfig.getTargetBusinessObject() == null
                    || referenceConfig.getTargetBusinessObject().getId() == null) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_REFERENCE",
                        "Reference configuration is missing for field " + field.getFieldKey()
                ));
                continue;
            }

            if (!referenceConfig.isAllowMultiple() && value instanceof Collection<?>) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_REFERENCE",
                        "Multiple references are not allowed for field " + field.getFieldKey()
                ));
                continue;
            }

            List<UUID> targetRecordIds = extractTargetRecordIds(value);

            if (targetRecordIds == null) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_REFERENCE",
                        "Invalid reference identifier for field " + field.getFieldKey()
                ));
                continue;
            }

            pendingValidations.put(
                    field,
                    new ParsedFieldReference(referenceConfig, targetRecordIds)
            );
            allReferencedRecordIds.addAll(targetRecordIds);
        }

        if (pendingValidations.isEmpty()) {
            return errors;
        }

        Map<UUID, BusinessObjectRecord> recordsById = new HashMap<>();

        if (!allReferencedRecordIds.isEmpty()) {
            List<BusinessObjectRecord> records =
                    recordRepository.findAllById(allReferencedRecordIds);

            if (records != null) {
                for (BusinessObjectRecord record : records) {
                    if (record != null && record.getId() != null) {
                        recordsById.put(record.getId(), record);
                    }
                }
            }
        }

        for (Map.Entry<BusinessObjectField, ParsedFieldReference> entry :
                pendingValidations.entrySet()) {
            BusinessObjectField field = entry.getKey();
            ParsedFieldReference parsed = entry.getValue();
            UUID expectedBusinessObjectId =
                    parsed.referenceConfig().getTargetBusinessObject().getId();

            for (UUID targetId : parsed.targetRecordIds()) {
                BusinessObjectRecord targetRecord = recordsById.get(targetId);

                if (targetRecord == null) {
                    errors.add(new ValidationError(
                            field.getFieldKey(),
                            "INVALID_REFERENCE",
                            "Referenced record " + targetId
                                    + " does not exist for field " + field.getFieldKey()
                    ));
                    break;
                }

                if (!expectedBusinessObjectId.equals(targetRecord.getBusinessObjectId())) {
                    errors.add(new ValidationError(
                            field.getFieldKey(),
                            "INVALID_REFERENCE",
                            "Referenced record " + targetId
                                    + " does not belong to target business object for field "
                                    + field.getFieldKey()
                    ));
                    break;
                }
            }
        }

        return errors;
    }

    private List<UUID> extractTargetRecordIds(Object value) {
        if (value instanceof UUID singleUuid) {
            return List.of(singleUuid);
        }

        if (value instanceof String singleString) {
            UUID parsed = parseUuid(singleString);
            return parsed == null ? null : List.of(parsed);
        }

        if (value instanceof Collection<?> collection) {
            List<UUID> ids = new ArrayList<>(collection.size());

            for (Object item : collection) {
                if (item instanceof UUID itemUuid) {
                    ids.add(itemUuid);
                } else if (item instanceof String itemString) {
                    UUID parsed = parseUuid(itemString);
                    if (parsed == null) {
                        return null;
                    }
                    ids.add(parsed);
                } else {
                    return null;
                }
            }

            return ids;
        }

        return null;
    }

    private UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private BusinessObjectFieldReference findReferenceConfig(
            BusinessObjectField field,
            Map<UUID, Optional<BusinessObjectFieldReference>> cache
    ) {
        UUID fieldId = field.getId();

        if (fieldId != null && cache.containsKey(fieldId)) {
            return cache.get(fieldId).orElse(null);
        }

        Optional<BusinessObjectFieldReference> referenceOpt = Optional.empty();

        if (fieldId != null) {
            referenceOpt = referenceRepository.findByFieldId(fieldId);
        }

        if (referenceOpt.isEmpty()) {
            referenceOpt = referenceRepository.findByField(field);
        }

        if (fieldId != null) {
            cache.put(fieldId, referenceOpt);
        }

        return referenceOpt.orElse(null);
    }

    private String normalizeFieldType(String fieldType) {
        return fieldType == null
                ? ""
                : fieldType.trim().toUpperCase(Locale.ROOT);
    }

    private record ParsedFieldReference(
            BusinessObjectFieldReference referenceConfig,
            List<UUID> targetRecordIds
    ) {
    }
}
