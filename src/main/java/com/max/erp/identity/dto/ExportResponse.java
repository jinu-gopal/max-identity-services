package com.max.erp.identity.dto;

import java.time.Instant;

public class ExportResponse {
    private String fileName;
    private String format;
    private int rowCount;
    private String content;
    private Instant exportedAt;

    public ExportResponse() {}

    public ExportResponse(String fileName, String format, int rowCount, String content, Instant exportedAt) {
        this.fileName = fileName;
        this.format = format;
        this.rowCount = rowCount;
        this.content = content;
        this.exportedAt = exportedAt;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getExportedAt() { return exportedAt; }
    public void setExportedAt(Instant exportedAt) { this.exportedAt = exportedAt; }
}
