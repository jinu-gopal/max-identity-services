package com.max.erp.identity.service;

import com.max.erp.identity.dto.ImportValidationErrorResponse;
import com.max.erp.identity.dto.ImportValidationResponse;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.ConfigurationImportError;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.entity.ConfigurationImportRow;
import com.max.erp.identity.enums.ImportJobStatus;
import com.max.erp.identity.enums.ImportRowStatus;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.ConfigurationImportErrorRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.repository.ConfigurationImportRowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImportValidationService {

    private final ConfigurationImportJobRepository importJobRepository;
    private final ConfigurationImportRowRepository importRowRepository;
    private final ConfigurationImportErrorRepository importErrorRepository;
    private final BusinessObjectFieldRepository fieldRepository;
    private final ImportFieldValueValidator fieldValueValidator;

    public ImportValidationService(
            ConfigurationImportJobRepository importJobRepository,
            ConfigurationImportRowRepository importRowRepository,
            ConfigurationImportErrorRepository importErrorRepository,
            BusinessObjectFieldRepository fieldRepository,
            ImportFieldValueValidator fieldValueValidator
    ) {
        this.importJobRepository = importJobRepository;
        this.importRowRepository = importRowRepository;
        this.importErrorRepository = importErrorRepository;
        this.fieldRepository = fieldRepository;
        this.fieldValueValidator = fieldValueValidator;
    }

    @Transactional
    public ImportValidationResponse validate(
            UUID businessObjectId,
            UUID importJobId) {

        ConfigurationImportJob job = importJobRepository.findById(importJobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import job not found"
                ));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Import job not found"
            );
        }

        Map<String, Object> rawMapping = job.getColumnMapping();

        if (rawMapping == null || rawMapping.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import mapping has not been configured"
            );
        }

        Map<String, String> mapping = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : rawMapping.entrySet()) {
            if (entry.getValue() != null) {
                mapping.put(
                        entry.getKey(),
                        String.valueOf(entry.getValue())
                );
            }
        }

        List<BusinessObjectField> fields =
                fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(
                        businessObjectId
                );

        Map<UUID, BusinessObjectField> fieldsById = new HashMap<>();

        for (BusinessObjectField field : fields) {
            fieldsById.put(field.getId(), field);
        }

        List<ConfigurationImportRow> rows =
                importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(
                        importJobId
                );

        if (rows.isEmpty()) {
            job.setStatus(ImportJobStatus.VALIDATED);
            job.setTotalRows(0);
            job.setValidRows(0);
            job.setInvalidRows(0);
            job.setDuplicateRows(0);
            job.setProcessedRows(0);
            importJobRepository.save(job);

            return buildResponse(job, List.of());
        }

        importErrorRepository.deleteAll(
                importErrorRepository.findAllByImportJobIdOrderByRowNumberAsc(
                        importJobId
                )
        );

        job.setStatus(ImportJobStatus.VALIDATING);
        job.setTotalRows(rows.size());
        job.setValidRows(0);
        job.setInvalidRows(0);
        job.setDuplicateRows(0);
        job.setProcessedRows(0);

        importJobRepository.save(job);

        List<ImportValidationErrorResponse> responseErrors =
                new ArrayList<>();

        for (ConfigurationImportRow row : rows) {

            row.setStatus(ImportRowStatus.PENDING);

            Map<String, Object> rawData =
                    row.getRawData() == null
                            ? Map.of()
                            : row.getRawData();

            Map<String, Object> normalizedData =
                    new LinkedHashMap<>();

            boolean rowValid = true;

            Set<UUID> mappedFieldIds = new HashSet<>();

            for (Map.Entry<String, String> mappingEntry : mapping.entrySet()) {

                String sourceColumn = mappingEntry.getKey();
                String fieldIdValue = mappingEntry.getValue();

                if (fieldIdValue == null || fieldIdValue.isBlank()) {
                    continue;
                }

                UUID fieldId;

                try {
                    fieldId = UUID.fromString(fieldIdValue);
                } catch (IllegalArgumentException ex) {

                    rowValid = false;

                    ImportValidationErrorResponse error =
                            addError(
                                    job,
                                    row,
                                    sourceColumn,
                                    "INVALID_FIELD_MAPPING",
                                    "Import mapping contains an invalid field identifier.",
                                    null
                            );

                    responseErrors.add(error);
                    continue;
                }

                BusinessObjectField field = fieldsById.get(fieldId);

                if (field == null) {
                    rowValid = false;

                    ImportValidationErrorResponse error =
                            addError(
                                    job,
                                    row,
                                    sourceColumn,
                                    "FIELD_NOT_FOUND",
                                    "Mapped Business Object field was not found.",
                                    null
                            );

                    responseErrors.add(error);
                    continue;
                }

                mappedFieldIds.add(fieldId);

                Object rawObject = rawData.get(sourceColumn);
                String rawValue = rawObject == null
                        ? null
                        : String.valueOf(rawObject);

                ImportFieldValueValidator.ValidationResult result =
                        fieldValueValidator.validate(
                                field,
                                rawValue
                        );

                if (!result.valid()) {

                    rowValid = false;

                    ImportValidationErrorResponse error =
                            addError(
                                    job,
                                    row,
                                    sourceColumn,
                                    result.errorCode(),
                                    result.errorMessage(),
                                    rawValue
                            );

                    responseErrors.add(error);

                } else if (result.normalizedValue() != null) {

                    normalizedData.put(
                            field.getFieldKey(),
                            result.normalizedValue()
                    );
                }
            }

            /*
             * Required fields must also be checked when no source column
             * has been mapped to them.
             */
            for (BusinessObjectField field : fields) {

                if (!field.isRequired()) {
                    continue;
                }

                if (field.isReadOnly() || field.isHidden()) {
                    continue;
                }

                if (mappedFieldIds.contains(field.getId())) {
                    continue;
                }

                rowValid = false;

                ImportValidationErrorResponse error =
                        addError(
                                job,
                                row,
                                null,
                                "REQUIRED_FIELD_NOT_MAPPED",
                                "Required field '" + field.getLabel()
                                        + "' is not mapped to an import column.",
                                null
                        );

                responseErrors.add(error);
            }

            row.setNormalizedData(normalizedData);

            if (rowValid) {
                row.setStatus(ImportRowStatus.VALID);
                job.setValidRows(job.getValidRows() + 1);
            } else {
                row.setStatus(ImportRowStatus.INVALID);
                job.setInvalidRows(job.getInvalidRows() + 1);
            }

            importRowRepository.save(row);
        }

        job.setStatus(ImportJobStatus.VALIDATED);

        importJobRepository.save(job);

        return buildResponse(job, responseErrors);
    }

    private ImportValidationErrorResponse addError(
            ConfigurationImportJob job,
            ConfigurationImportRow row,
            String columnName,
            String errorCode,
            String errorMessage,
            String sourceValue) {

        ConfigurationImportError entity =
                new ConfigurationImportError();

        entity.setImportJobId(job.getId());
        entity.setImportRowId(row.getId());
        entity.setRowNumber(row.getRowNumber());
        entity.setColumnName(columnName);
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(errorMessage);
        entity.setSourceValue(sourceValue);

        importErrorRepository.save(entity);

        ImportValidationErrorResponse response =
                new ImportValidationErrorResponse();

        response.setRowNumber(row.getRowNumber());
        response.setColumnName(columnName);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        response.setSourceValue(sourceValue);

        return response;
    }

    private ImportValidationResponse buildResponse(
            ConfigurationImportJob job,
            List<ImportValidationErrorResponse> errors) {

        ImportValidationResponse response =
                new ImportValidationResponse();

        response.setImportJobId(job.getId());
        response.setBusinessObjectId(job.getBusinessObjectId());
        response.setStatus(job.getStatus());
        response.setTotalRows(job.getTotalRows());
        response.setValidRows(job.getValidRows());
        response.setInvalidRows(job.getInvalidRows());
        response.setDuplicateRows(job.getDuplicateRows());
        response.setErrors(errors);

        return response;
    }
}
