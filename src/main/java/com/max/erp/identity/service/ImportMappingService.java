package com.max.erp.identity.service;

import com.max.erp.identity.dto.ImportMappingResponse;
import com.max.erp.identity.dto.ImportMappingSaveRequest;
import com.max.erp.identity.dto.ImportMappingSaveResponse;
import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.ConfigurationImportJob;
import com.max.erp.identity.entity.ConfigurationImportRow;
import com.max.erp.identity.repository.BusinessObjectFieldRepository;
import com.max.erp.identity.repository.ConfigurationImportJobRepository;
import com.max.erp.identity.repository.ConfigurationImportRowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImportMappingService {

    private final ConfigurationImportJobRepository importJobRepository;
    private final ConfigurationImportRowRepository importRowRepository;
    private final BusinessObjectFieldRepository fieldRepository;

    public ImportMappingService(
            ConfigurationImportJobRepository importJobRepository,
            ConfigurationImportRowRepository importRowRepository,
            BusinessObjectFieldRepository fieldRepository) {

        this.importJobRepository = importJobRepository;
        this.importRowRepository = importRowRepository;
        this.fieldRepository = fieldRepository;
    }

    @Transactional(readOnly = true)
    public ImportMappingResponse getMappingPreview(
            UUID businessObjectId,
            UUID importJobId) {

        ConfigurationImportJob job =
                importJobRepository.findById(importJobId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Import job not found"));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import job does not belong to the specified business object");
        }

        List<ConfigurationImportRow> rows =
                importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(
                        importJobId);

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import contains no parsed rows");
        }

        ConfigurationImportRow firstRow = rows.get(0);

        List<String> sourceColumns =
                new ArrayList<>(firstRow.getRawData().keySet());

        List<BusinessObjectField> fields =
                fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(
                        businessObjectId);

        List<ImportMappingResponse.TargetField> targetFields =
                fields.stream()
                        .map(this::toTargetField)
                        .toList();

        List<ImportMappingResponse.MappingCandidate> candidates =
                buildCandidates(sourceColumns, fields);

        return new ImportMappingResponse(
                job.getId(),
                job.getBusinessObjectId(),
                sourceColumns,
                targetFields,
                candidates
        );
    }


    @Transactional
    public ImportMappingSaveResponse saveMapping(
            UUID businessObjectId,
            UUID importJobId,
            ImportMappingSaveRequest request) {

        ConfigurationImportJob job =
                importJobRepository.findById(importJobId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Import job not found"));

        if (!businessObjectId.equals(job.getBusinessObjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import job does not belong to the specified business object");
        }

        if (request == null || request.mapping() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mapping is required");
        }

        List<ConfigurationImportRow> rows =
                importRowRepository.findAllByImportJobIdOrderByRowNumberAsc(
                        importJobId);

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import contains no parsed rows");
        }

        Set<String> sourceColumns =
                rows.get(0).getRawData().keySet();

        List<BusinessObjectField> fields =
                fieldRepository.findAllByBusinessObjectIdOrderByDisplayOrderAsc(
                        businessObjectId);

        Map<UUID, BusinessObjectField> fieldsById =
                new HashMap<>();

        for (BusinessObjectField field : fields) {
            fieldsById.put(field.getId(), field);
        }

        Map<String, UUID> validatedMapping =
                new LinkedHashMap<>();

        for (Map.Entry<String, UUID> entry :
                request.mapping().entrySet()) {

            String sourceColumn = entry.getKey();
            UUID targetFieldId = entry.getValue();

            if (sourceColumn == null ||
                    sourceColumn.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Mapping contains an empty source column");
            }

            if (!sourceColumns.contains(sourceColumn)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Source column not found in import: "
                                + sourceColumn);
            }

            if (targetFieldId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Target field is required for source column: "
                                + sourceColumn);
            }

            BusinessObjectField targetField =
                    fieldsById.get(targetFieldId);

            if (targetField == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Target field does not belong to the specified business object: "
                                + targetFieldId);
            }

            if (targetField.isReadOnly() ||
                    targetField.isHidden()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Target field cannot be mapped: "
                                + targetField.getFieldKey());
            }

            validatedMapping.put(
                    sourceColumn,
                    targetFieldId
            );
        }

        Set<UUID> mappedTargetFields =
                new HashSet<>();

        for (UUID targetFieldId : validatedMapping.values()) {

            if (!mappedTargetFields.add(targetFieldId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Multiple source columns cannot map to the same target field: "
                                + targetFieldId);
            }
        }

        Map<String, Object> storedMapping =
                new LinkedHashMap<>();

        for (Map.Entry<String, UUID> entry :
                validatedMapping.entrySet()) {

            storedMapping.put(
                    entry.getKey(),
                    entry.getValue().toString()
            );
        }

        job.setColumnMapping(storedMapping);

        importJobRepository.save(job);

        return new ImportMappingSaveResponse(
                job.getId(),
                job.getBusinessObjectId(),
                validatedMapping
        );
    }

    private List<ImportMappingResponse.MappingCandidate> buildCandidates(
            List<String> sourceColumns,
            List<BusinessObjectField> fields) {

        List<ImportMappingResponse.MappingCandidate> candidates =
                new ArrayList<>();

        Set<UUID> alreadyMatchedFields =
                new HashSet<>();

        Map<String, BusinessObjectField> fieldsByKey =
                new HashMap<>();

        Map<String, BusinessObjectField> fieldsByLabel =
                new HashMap<>();

        for (BusinessObjectField field : fields) {

            if (field.isReadOnly() || field.isHidden()) {
                continue;
            }

            fieldsByKey.put(
                    normalize(field.getFieldKey()),
                    field);

            fieldsByLabel.put(
                    normalize(field.getLabel()),
                    field);
        }

        for (String sourceColumn : sourceColumns) {

            String normalizedSource =
                    normalize(sourceColumn);

            BusinessObjectField matchedField =
                    fieldsByKey.get(normalizedSource);

            String matchType = "FIELD_KEY";

            if (matchedField == null) {
                matchedField =
                        fieldsByLabel.get(normalizedSource);

                matchType = "LABEL";
            }

            if (matchedField == null) {
                continue;
            }

            if (!alreadyMatchedFields.add(matchedField.getId())) {
                continue;
            }

            candidates.add(
                    new ImportMappingResponse.MappingCandidate(
                            sourceColumn,
                            matchedField.getId(),
                            matchedField.getFieldKey(),
                            matchedField.getLabel(),
                            matchType
                    )
            );
        }

        return candidates;
    }

    private ImportMappingResponse.TargetField toTargetField(
            BusinessObjectField field) {

        return new ImportMappingResponse.TargetField(
                field.getId(),
                field.getFieldKey(),
                field.getLabel(),
                field.getFieldType(),
                field.isRequired(),
                field.isUniqueValue(),
                field.isReadOnly(),
                field.isHidden()
        );
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
