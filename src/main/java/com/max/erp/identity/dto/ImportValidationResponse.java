package com.max.erp.identity.dto;

import com.max.erp.identity.enums.ImportJobStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImportValidationResponse {

    private UUID importJobId;
    private UUID businessObjectId;
    private ImportJobStatus status;

    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int duplicateRows;

    private List<ImportValidationErrorResponse> errors =
            new ArrayList<>();

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

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public int getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(int duplicateRows) {
        this.duplicateRows = duplicateRows;
    }

    public List<ImportValidationErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(
            List<ImportValidationErrorResponse> errors
    ) {
        this.errors = errors;
    }
}
