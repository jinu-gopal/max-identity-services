package com.max.erp.identity.service;

import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.validation.RecordValidationEngine;
import com.max.erp.identity.validation.RecordValidationException;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationOperation;
import com.max.erp.identity.validation.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BusinessObjectRecordService {

    private final BusinessObjectRecordRepository recordRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final BusinessObjectFieldRepository fieldRepository;
    private final RecordValidationEngine validationEngine;

    public BusinessObjectRecordService(
            BusinessObjectRecordRepository recordRepository,
            BusinessObjectRepository businessObjectRepository,
            BusinessObjectFieldRepository fieldRepository,
            RecordValidationEngine validationEngine
    ) {
        this.recordRepository = recordRepository;
        this.businessObjectRepository = businessObjectRepository;
        this.fieldRepository = fieldRepository;
        this.validationEngine = validationEngine;
    }

    public List<BusinessObjectRecord> getRecords(UUID businessObjectId) {
        return recordRepository
                .findByBusinessObjectIdOrderByCreatedAtDesc(businessObjectId);
    }

    public BusinessObjectRecord getRecord(
            UUID businessObjectId,
            UUID recordId
    ) {
        BusinessObjectRecord record = recordRepository
                .findById(recordId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Record not found"
                        )
                );

        if (!record.getBusinessObjectId().equals(businessObjectId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Record not found"
            );
        }

        return record;
    }

    public BusinessObjectRecord createRecord(
            UUID businessObjectId,
            Map<String, Object> recordData,
            UUID createdBy
    ) {
        if (recordData == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Record data is required"
            );
        }

        BusinessObject businessObject = getBusinessObject(businessObjectId);
        List<BusinessObjectField> fields = fieldRepository
                .findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId);

        validateRecordData(
                businessObject,
                fields,
                recordData,
                ValidationOperation.CREATE,
                null
        );

        OffsetDateTime now = OffsetDateTime.now();

        BusinessObjectRecord record = new BusinessObjectRecord();

        record.setId(UUID.randomUUID());
        record.setBusinessObjectId(businessObjectId);
        record.setRecordData(recordData);
        record.setCreatedBy(createdBy);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        return recordRepository.save(record);
    }

    public BusinessObjectRecord updateRecord(
            UUID businessObjectId,
            UUID recordId,
            Map<String, Object> recordData
    ) {
        if (recordData == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Record data is required"
            );
        }

        BusinessObjectRecord record = getRecord(
                businessObjectId,
                recordId
        );

        BusinessObject businessObject = getBusinessObject(businessObjectId);
        List<BusinessObjectField> fields = fieldRepository
                .findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId);

        Map<String, Object> mergedRecordData = new LinkedHashMap<>();
        if (record.getRecordData() != null) {
            mergedRecordData.putAll(record.getRecordData());
        }
        mergedRecordData.putAll(recordData);

        validateRecordData(
                businessObject,
                fields,
                mergedRecordData,
                ValidationOperation.UPDATE,
                recordId
        );

        record.setRecordData(mergedRecordData);
        record.setUpdatedAt(OffsetDateTime.now());

        return recordRepository.save(record);
    }

    public void deleteRecord(
            UUID businessObjectId,
            UUID recordId
    ) {
        BusinessObjectRecord record = getRecord(
                businessObjectId,
                recordId
        );

        recordRepository.delete(record);
    }

    private BusinessObject getBusinessObject(UUID businessObjectId) {
        return businessObjectRepository
                .findById(businessObjectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Business object not found"
                        )
                );
    }

    private void validateRecordData(
            BusinessObject businessObject,
            List<BusinessObjectField> fields,
            Map<String, Object> values,
            ValidationOperation operation,
            UUID recordId
    ) {
        ValidationContext context = new ValidationContext(
                businessObject,
                fields,
                values,
                operation,
                recordId
        );

        ValidationResult result = validationEngine.validate(context);

        if (!result.valid()) {
            throw new RecordValidationException(result.errors());
        }
    }
}