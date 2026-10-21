package com.max.erp.identity.dto;

public class FormLayoutFieldDto {
    private String fieldKey;
    private String label;
    private String fieldType; // TEXT, NUMBER, BOOLEAN, CHOICE, REFERENCE, DATE
    private boolean required;
    private boolean readOnly;
    private int colSpan; // 1 or 2
    private String defaultValue;

    public FormLayoutFieldDto() {
        this.colSpan = 1;
    }

    public FormLayoutFieldDto(String fieldKey, String label, String fieldType, boolean required, boolean readOnly, int colSpan, String defaultValue) {
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.required = required;
        this.readOnly = readOnly;
        this.colSpan = colSpan;
        this.defaultValue = defaultValue;
    }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

    public int getColSpan() { return colSpan; }
    public void setColSpan(int colSpan) { this.colSpan = colSpan; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
}
