package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@Order(80)
public class UniqueValueValidator implements RecordValidator {

    private final BusinessObjectRecordRepository recordRepository;

    public UniqueValueValidator(BusinessObjectRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        if (values == null || context.fields() == null) {
            return errors;
        }

        Map<UUID, List<BusinessObjectRecord>> existingRecordsCache = new HashMap<>();

        for (BusinessObjectField field : context.fields()) {
            if (!field.isUniqueValue()) {
                continue;
            }

            Object submittedValue = values.get(field.getFieldKey());

            if (isEmptyValue(submittedValue)) {
                continue;
            }

            UUID businessObjectId = resolveBusinessObjectId(context, field);

            if (businessObjectId == null) {
                continue;
            }

            List<BusinessObjectRecord> existingRecords =
                    getExistingRecords(businessObjectId, existingRecordsCache);

            for (BusinessObjectRecord existingRecord : existingRecords) {
                if (existingRecord == null) {
                    continue;
                }

                if (context.recordId() != null
                        && context.recordId().equals(existingRecord.getId())) {
                    continue;
                }

                Map<String, Object> existingData = existingRecord.getRecordData();

                if (existingData == null) {
                    continue;
                }

                Object existingValue = existingData.get(field.getFieldKey());

                if (isEmptyValue(existingValue)) {
                    continue;
                }

                if (areValuesEqual(submittedValue, existingValue)) {
                    errors.add(new ValidationError(
                            field.getFieldKey(),
                            "DUPLICATE_VALUE",
                            "Value for field " + field.getFieldKey() + " must be unique"
                    ));
                    break;
                }
            }
        }

        return errors;
    }

    private List<BusinessObjectRecord> getExistingRecords(
            UUID businessObjectId,
            Map<UUID, List<BusinessObjectRecord>> cache
    ) {
        if (cache.containsKey(businessObjectId)) {
            return cache.get(businessObjectId);
        }

        List<BusinessObjectRecord> records =
                recordRepository.findByBusinessObjectIdOrderByCreatedAtDesc(
                        businessObjectId
                );

        List<BusinessObjectRecord> safeRecords =
                records != null ? records : Collections.emptyList();

        cache.put(businessObjectId, safeRecords);
        return safeRecords;
    }

    private UUID resolveBusinessObjectId(
            ValidationContext context,
            BusinessObjectField field
    ) {
        if (context.businessObject() != null
                && context.businessObject().getId() != null) {
            return context.businessObject().getId();
        }

        if (field.getBusinessObject() != null
                && field.getBusinessObject().getId() != null) {
            return field.getBusinessObject().getId();
        }

        return null;
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String stringValue) {
            return stringValue.isBlank();
        }

        if (value instanceof Collection<?> collectionValue) {
            return collectionValue.isEmpty();
        }

        return false;
    }

    private boolean areValuesEqual(Object submittedValue, Object existingValue) {
        BigDecimal submittedNumber = toBigDecimal(submittedValue);
        BigDecimal existingNumber = toBigDecimal(existingValue);

        if (submittedNumber != null && existingNumber != null) {
            return submittedNumber.compareTo(existingNumber) == 0;
        }

        if (submittedValue instanceof String submittedString
                && existingValue instanceof String existingString) {
            return submittedString.trim().equals(existingString.trim());
        }

        if (submittedValue instanceof UUID submittedUuid
                && existingValue instanceof UUID existingUuid) {
            return submittedUuid.equals(existingUuid);
        }

        if (submittedValue instanceof UUID submittedUuid
                && existingValue instanceof String existingString) {
            return submittedUuid.toString().equalsIgnoreCase(existingString.trim());
        }

        if (submittedValue instanceof String submittedString
                && existingValue instanceof UUID existingUuid) {
            return submittedString.trim().equalsIgnoreCase(existingUuid.toString());
        }

        return Objects.equals(submittedValue, existingValue);
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
}
