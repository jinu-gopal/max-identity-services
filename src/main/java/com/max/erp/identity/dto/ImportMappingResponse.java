package com.max.erp.identity.dto;

import java.util.List;
import java.util.UUID;

public record ImportMappingResponse(
        UUID importJobId,
        UUID businessObjectId,
        List<String> sourceColumns,
        List<TargetField> targetFields,
        List<MappingCandidate> mappingCandidates
) {

    public record TargetField(
            UUID fieldId,
            String fieldKey,
            String label,
            String fieldType,
            boolean required,
            boolean uniqueValue,
            boolean readOnly,
            boolean hidden
    ) {
    }

    public record MappingCandidate(
            String sourceColumn,
            UUID targetFieldId,
            String targetFieldKey,
            String targetLabel,
            String matchType
    ) {
    }
}
