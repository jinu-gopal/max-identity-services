package com.max.erp.identity.validation;

import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ValidationContext(
        BusinessObject businessObject,
        List<BusinessObjectField> fields,
        Map<String, Object> values,
        ValidationOperation operation,
        UUID recordId
) {
    public ValidationContext(
            BusinessObject businessObject,
            List<BusinessObjectField> fields,
            Map<String, Object> values,
            ValidationOperation operation
    ) {
        this(businessObject, fields, values, operation, null);
    }
}
