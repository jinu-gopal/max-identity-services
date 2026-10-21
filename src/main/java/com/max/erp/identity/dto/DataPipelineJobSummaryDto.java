package com.max.erp.identity.dto;

import java.time.Instant;
import java.util.UUID;

public class DataPipelineJobSummaryDto {
    private UUID jobId;
    private String fileName;
    private UUID businessObjectId;
    private String businessObjectName;
    private String status;
    private int totalRows;
    private int validRows;
    private int errorRows;
    private int importedRows;
    private Instant createdAt;
    private Instant completedAt;

    public DataPipelineJobSummaryDto() {}

    public DataPipelineJobSummaryDto(UUID jobId, String fileName, UUID businessObjectId,
                                     String businessObjectName, String status, int totalRows,
                                     int validRows, int errorRows, int importedRows,
                                     Instant createdAt, Instant completedAt) {
        this.jobId = jobId;
        this.fileName = fileName;
        this.businessObjectId = businessObjectId;
        this.businessObjectName = businessObjectName;
        this.status = status;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.errorRows = errorRows;
        this.importedRows = importedRows;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public UUID getBusinessObjectId() { return businessObjectId; }
    public void setBusinessObjectId(UUID businessObjectId) { this.businessObjectId = businessObjectId; }

    public String getBusinessObjectName() { return businessObjectName; }
    public void setBusinessObjectName(String businessObjectName) { this.businessObjectName = businessObjectName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getValidRows() { return validRows; }
    public void setValidRows(int validRows) { this.validRows = validRows; }

    public int getErrorRows() { return errorRows; }
    public void setErrorRows(int errorRows) { this.errorRows = errorRows; }

    public int getImportedRows() { return importedRows; }
    public void setImportedRows(int importedRows) { this.importedRows = importedRows; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
