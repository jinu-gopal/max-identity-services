package com.max.erp.identity.dto;

public class DataPipelineErrorRowDto {
    private int rowNumber;
    private String columnName;
    private String errorCode;
    private String errorMessage;
    private String rawValue;

    public DataPipelineErrorRowDto() {}

    public DataPipelineErrorRowDto(int rowNumber, String columnName, String errorCode, String errorMessage, String rawValue) {
        this.rowNumber = rowNumber;
        this.columnName = columnName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.rawValue = rawValue;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
}
