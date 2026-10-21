package com.max.erp.identity.dto;

import com.max.erp.identity.enums.ImportJobStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImportExecutionResponse {

    private UUID importJobId;
    private UUID businessObjectId;
    private ImportJobStatus status;

    private int totalRows;
    private int processedRows;
    private int failedRows;
    private int duplicateRows;

    private List<UUID> createdRecordIds = new ArrayList<>();
    private List<ImportValidationErrorResponse> errors = new ArrayList<>();

    private Instant startedAt;
    private Instant completedAt;

    public UUID getImportJobId() {
        return importJobId;
    }

    public void setImportJobId(UUID importJobId) {
        this.importJobId = importJobId;
    }

    public UUID getBusinessObjectId() {
        return businessObjectId;
    }

    public void setBusinessObjectId(UUID businessObjectId) {
        this.businessObjectId = businessObjectId;
    }

    public ImportJobStatus getStatus() {
        return status;
    }

    public void setStatus(ImportJobStatus status) {
        this.status = status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(int processedRows) {
        this.processedRows = processedRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }

    public int getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(int duplicateRows) {
        this.duplicateRows = duplicateRows;
    }

    public List<UUID> getCreatedRecordIds() {
        return createdRecordIds;
    }

    public void setCreatedRecordIds(List<UUID> createdRecordIds) {
        this.createdRecordIds = createdRecordIds;
    }

    public List<ImportValidationErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportValidationErrorResponse> errors) {
        this.errors = errors;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
