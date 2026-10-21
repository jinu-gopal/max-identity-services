package com.max.erp.identity.service;

import com.max.erp.identity.dto.ImportExecutionResponse;
import com.max.erp.identity.dto.ImportValidationErrorResponse;
import com.max.erp.identity.entity.BusinessObject;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.BusinessObjectRecord;
import com.max.erp.identity.entity.ConfigurationImportError;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.entity.ConfigurationImportRow;
import com.max.erp.identity.enums.ImportJobStatus;
import com.max.erp.identity.enums.ImportRowStatus;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.BusinessObjectRecordRepository;
import com.max.erp.identity.repository.BusinessObjectRepository;
import com.max.erp.identity.repository.ConfigurationImportErrorRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.repository.ConfigurationImportRowRepository;
import com.max.erp.identity.validation.RecordValidationEngine;
import com.max.erp.identity.validation.ValidationError;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationOperation;
import com.max.erp.identity.validation.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImportExecutionService {

    private final ConfigurationImportJobRepository importJobRepository;
    private final ConfigurationImportRowRepository importRowRepository;
    private final ConfigurationImportErrorRepository importErrorRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final BusinessObjectFieldRepository fieldRepository;
    private final BusinessObjectRecordRepository recordRepository;
    private final RecordValidationEngine validationEngine;
    private final ImportFieldValueValidator fieldValueValidator;

    public ImportExecutionService(
            ConfigurationImportJobRepository importJobRepository,
            ConfigurationImportRowRepository importRowRepository,
            ConfigurationImportErrorRepository importErrorRepository,
            BusinessObjectRepository businessObjectRepository,
            BusinessObjectFieldRepository fieldRepository,
            BusinessObjectRecordRepository recordRepository,
            RecordValidationEngine validationEngine,
            ImportFieldValueValidator fieldValueValidator
    ) {
        this.importJobRepository = importJobRepository;
        this.importRowRepository = importRowRepository;
        this.importErrorRepository = importErrorRepository;
        this.businessObjectRepository = businessObjectRepository;
        this.fieldRepository = fieldRepository;
        this.recordRepository = recordRepository;
        this.validationEngine = validationEngine;
        this.fieldValueValidator = fieldValueValidator;
    }

    @Transactional
    public ImportExecutionResponse execute(
            UUID businessObjectId,
            UUID importJobId
    ) {
        return execute(businessObjectId, importJobId, null);
    }

    @Transactional
    public ImportExecutionResponse execute(
            UUID businessObjectId,
            UUID importJobId,
            UUID userId
    ) {
        if (businessObjectId == null || importJobId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Business object ID and import job ID are required"
            );
        }

        ConfigurationImportJob job = importJobRepository.findById(importJobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import job not found"
                ));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Import job does not belong to the specified business object"
            );
        }

        // Fast-path status checks for clear error responses
        if (job.getStatus() == ImportJobStatus.EXECUTING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Import job is currently executing"
            );
        }

        if (job.getStatus() == ImportJobStatus.COMPLETED
                || (job.getStatus() == ImportJobStatus.COMPLETED_WITH_ERRORS && (job.getProcessedRows() != null && job.getProcessedRows() > 0))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Import job has already been executed"
            );
        }

        boolean wasUnexecutedCompletedWithErrors =
                job.getStatus() == ImportJobStatus.COMPLETED_WITH_ERRORS
                && (job.getProcessedRows() == null || job.getProcessedRows() == 0);

        if (job.getStatus() != ImportJobStatus.VALIDATED && !wasUnexecutedCompletedWithErrors) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Import job is not in a VALIDATED state (current status: "
                            + job.getStatus() + ")"
            );
        }

        // Atomic conditional state transition to prevent concurrent execution
        Instant now = Instant.now();
        int updated = importJobRepository.updateStatusIfStatus(
                importJobId,
                ImportJobStatus.EXECUTING,
                now,
                now,
                wasUnexecutedCompletedWithErrors ? ImportJobStatus.COMPLETED_WITH_ERRORS : ImportJobStatus.VALIDATED
        );

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Import job is already executing or status was modified concurrently"
            );
        }

        job.setStatus(ImportJobStatus.EXECUTING);
        job.setStartedAt(now);

        // Verify business object lifecycle
        BusinessObject businessObject = businessObjectRepository.findById(businessObjectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business object not found"
                ));

        if (businessObject.getStatus() != null
                && "ARCHIVED".equalsIgnoreCase(businessObject.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Business object is archived and cannot receive imported records"
            );
        }

        // Verify active fields and detect schema drift since validation
        List<BusinessObjectField> allFields = fieldRepository
                .findAllByBusinessObjectIdOrderByDisplayOrderAsc(businessObjectId);

        List<BusinessObjectField> activeFields = allFields.stream()
                .filter(f -> !f.isHidden())
                .toList();

        Map<UUID, BusinessObjectField> fieldsById = new HashMap<>();
        for (BusinessObjectField f : activeFields) {
            fieldsById.put(f.getId(), f);
        }

        // Verify mapping presence and validity against live schema
        Map<String, Object> rawMapping = job.getColumnMapping();
        if (rawMapping == null || rawMapping.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import mapping has not been configured"
            );
        }

        Map<String, UUID> mapping = new LinkedHashMap<>();
        Set<UUID> mappedFieldIds = new HashSet<>();

        for (Map.Entry<String, Object> entry : rawMapping.entrySet()) {
            String sourceCol = entry.getKey();
            Object val = entry.getValue();

            if (val == null || String.valueOf(val).isBlank()) {
                continue;
            }

            UUID fieldId;
            try {
                fieldId = UUID.fromString(String.valueOf(val));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Schema changed since validation: Invalid field ID in mapping: " + val
                );
            }

            BusinessObjectField field = fieldsById.get(fieldId);
            if (field == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Schema changed since validation: Mapped field was not found in business object: " + fieldId
                );
            }

            if (field.isReadOnly()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Schema changed since validation: Mapped field '" + field.getFieldKey() + "' is read-only"
                );
            }

            mapping.put(sourceCol, fieldId);
            mappedFieldIds.add(fieldId);
        }

        // Verify all required non-readonly fields are mapped
        for (BusinessObjectField field : activeFields) {
            if (field.isRequired() && !field.isReadOnly() && !field.isHidden()) {
                if (!mappedFieldIds.contains(field.getId())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Schema changed since validation: Required field '" + field.getLabel() + "' is not mapped"
                    );
                }
            }
        }

        // Retrieve import rows and identify valid candidates
        List<ConfigurationImportRow> rows = importRowRepository
                .findAllByImportJobIdOrderByRowNumberAsc(importJobId);

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import contains no rows to execute"
            );
        }

        List<ConfigurationImportRow> candidateRows = rows.stream()
                .filter(r -> r.getStatus() == ImportRowStatus.VALID)
                .toList();

        if (candidateRows.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import contains no valid rows to execute"
            );
        }

        // Set up in-memory intra-batch uniqueness tracker
        Map<String, Set<Object>> seenUniqueValues = new HashMap<>();
        for (BusinessObjectField field : activeFields) {
            if (field.isUniqueValue()) {
                seenUniqueValues.put(field.getFieldKey(), new HashSet<>());
            }
        }

        List<UUID> createdRecordIds = new ArrayList<>();
        List<ImportValidationErrorResponse> executionErrors = new ArrayList<>();
        int processedCount = 0;
        int failedCount = 0;
        int duplicateCount = 0;

        for (ConfigurationImportRow row : candidateRows) {
            // Idempotency: skip rows that were already imported
            if (row.getStatus() == ImportRowStatus.IMPORTED) {
                continue;
            }

            Map<String, Object> recordData = buildRecordData(row, mapping, fieldsById);

            // Check 1: Intra-batch uniqueness conflict
            boolean intraBatchDuplicate = false;
            for (Map.Entry<String, Set<Object>> entry : seenUniqueValues.entrySet()) {
                String uniqueKey = entry.getKey();
                Object val = recordData.get(uniqueKey);

                if (val != null && !isEmptyValue(val)) {
                    Object normalizedVal = normalizeForUniqueness(val);
                    if (entry.getValue().contains(normalizedVal)) {
                        intraBatchDuplicate = true;
                        row.setStatus(ImportRowStatus.DUPLICATE);
                        duplicateCount++;

                        ImportValidationErrorResponse err = recordError(
                                job,
                                row,
                                uniqueKey,
                                "DUPLICATE_VALUE",
                                "Duplicate value '" + val + "' for unique field '" + uniqueKey + "' detected within the import batch.",
                                String.valueOf(val)
                        );
                        executionErrors.add(err);
                        break;
                    }
                }
            }

            if (intraBatchDuplicate) {
                importRowRepository.save(row);
                continue;
            }

            // Check 2: Live revalidation through authoritative RecordValidationEngine
            ValidationContext context = new ValidationContext(
                    businessObject,
                    activeFields,
                    recordData,
                    ValidationOperation.CREATE,
                    null
            );

            ValidationResult validationResult = validationEngine.validate(context);

            if (!validationResult.valid()) {
                boolean isDuplicate = validationResult.errors().stream()
                        .anyMatch(e -> "DUPLICATE_VALUE".equals(e.code()));

                if (isDuplicate) {
                    row.setStatus(ImportRowStatus.DUPLICATE);
                    duplicateCount++;
                } else {
                    row.setStatus(ImportRowStatus.FAILED);
                    failedCount++;
                }

                for (ValidationError err : validationResult.errors()) {
                    ImportValidationErrorResponse errResp = recordError(
                            job,
                            row,
                            err.fieldKey(),
                            err.code(),
                            err.message(),
                            recordData.get(err.fieldKey()) != null
                                    ? String.valueOf(recordData.get(err.fieldKey()))
                                    : null
                    );
                    executionErrors.add(errResp);
                }

                importRowRepository.save(row);
                continue;
            }

            // Record values in seenUniqueValues tracker
            for (Map.Entry<String, Set<Object>> entry : seenUniqueValues.entrySet()) {
                Object val = recordData.get(entry.getKey());
                if (val != null && !isEmptyValue(val)) {
                    entry.getValue().add(normalizeForUniqueness(val));
                }
            }

            // Persist generic BusinessObjectRecord
            OffsetDateTime recordTimestamp = OffsetDateTime.now();
            BusinessObjectRecord record = new BusinessObjectRecord();
            record.setId(UUID.randomUUID());
            record.setBusinessObjectId(businessObjectId);
            record.setRecordData(recordData);
            record.setCreatedBy(userId != null ? userId : job.getCreatedBy());
            record.setCreatedAt(recordTimestamp);
            record.setUpdatedAt(recordTimestamp);

            BusinessObjectRecord savedRecord = recordRepository.save(record);
            createdRecordIds.add(savedRecord.getId());

            row.setStatus(ImportRowStatus.IMPORTED);
            importRowRepository.save(row);
            processedCount++;
        }

        // Finalize job status and metrics
        job.setProcessedRows(processedCount);
        job.setDuplicateRows(job.getDuplicateRows() + duplicateCount);
        job.setInvalidRows(job.getInvalidRows() + failedCount);
        job.setCompletedAt(Instant.now());

        if (failedCount == 0 && duplicateCount == 0) {
            job.setStatus(ImportJobStatus.COMPLETED);
        } else {
            job.setStatus(ImportJobStatus.COMPLETED_WITH_ERRORS);
        }

        Map<String, Object> summary = job.getSummary() != null
                ? new LinkedHashMap<>(job.getSummary())
                : new LinkedHashMap<>();

        summary.put("importedRows", processedCount);
        summary.put("failedRows", failedCount);
        summary.put("duplicateRows", duplicateCount);
        summary.put("createdRecordIds", createdRecordIds.stream().map(UUID::toString).toList());
        summary.put("completedAt", job.getCompletedAt().toString());

        job.setSummary(summary);
        importJobRepository.save(job);

        // Build execution response
        ImportExecutionResponse response = new ImportExecutionResponse();
        response.setImportJobId(job.getId());
        response.setBusinessObjectId(businessObjectId);
        response.setStatus(job.getStatus());
        response.setTotalRows(job.getTotalRows());
        response.setProcessedRows(processedCount);
        response.setFailedRows(failedCount);
        response.setDuplicateRows(duplicateCount);
        response.setCreatedRecordIds(createdRecordIds);
        response.setErrors(executionErrors);
        response.setStartedAt(job.getStartedAt());
        response.setCompletedAt(job.getCompletedAt());

        return response;
    }

    private Map<String, Object> buildRecordData(
            ConfigurationImportRow row,
            Map<String, UUID> mapping,
            Map<UUID, BusinessObjectField> fieldsById
    ) {
        Map<String, Object> recordData = new LinkedHashMap<>();
        Map<String, Object> normalizedData = row.getNormalizedData();
        Map<String, Object> rawData = row.getRawData() != null ? row.getRawData() : Map.of();

        for (Map.Entry<String, UUID> entry : mapping.entrySet()) {
            String sourceColumn = entry.getKey();
            UUID fieldId = entry.getValue();
            BusinessObjectField field = fieldsById.get(fieldId);

            if (field == null) {
                continue;
            }

            Object value = null;
            if (normalizedData != null && normalizedData.containsKey(field.getFieldKey())) {
                value = normalizedData.get(field.getFieldKey());
            } else if (rawData.containsKey(sourceColumn)) {
                Object rawObject = rawData.get(sourceColumn);
                String rawStr = rawObject == null ? null : String.valueOf(rawObject);
                ImportFieldValueValidator.ValidationResult result =
                        fieldValueValidator.validate(field, rawStr);
                if (result.valid()) {
                    value = result.normalizedValue();
                }
            }

            if (value != null) {
                recordData.put(field.getFieldKey(), value);
            }
        }

        return recordData;
    }

    private ImportValidationErrorResponse recordError(
            ConfigurationImportJob job,
            ConfigurationImportRow row,
            String columnName,
            String errorCode,
            String errorMessage,
            String sourceValue
    ) {
        ConfigurationImportError entity = new ConfigurationImportError();
        entity.setImportJobId(job.getId());
        entity.setImportRowId(row.getId());
        entity.setRowNumber(row.getRowNumber());
        entity.setColumnName(columnName);
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(errorMessage);
        entity.setSourceValue(sourceValue);

        importErrorRepository.save(entity);

        ImportValidationErrorResponse response = new ImportValidationErrorResponse();
        response.setRowNumber(row.getRowNumber());
        response.setColumnName(columnName);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        response.setSourceValue(sourceValue);

        return response;
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isBlank();
        }
        if (value instanceof Collection<?> c) {
            return c.isEmpty();
        }
        return false;
    }

    private Object normalizeForUniqueness(Object value) {
        if (value instanceof String s) {
            return s.trim();
        }
        if (value instanceof Number) {
            try {
                return new BigDecimal(value.toString()).stripTrailingZeros();
            } catch (Exception ex) {
                return value;
            }
        }
        return value;
    }
}
