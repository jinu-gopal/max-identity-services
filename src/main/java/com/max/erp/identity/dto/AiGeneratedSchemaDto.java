package com.max.erp.identity.dto;

import java.util.ArrayList;
import java.util.List;

public class AiGeneratedSchemaDto {
    private String objectKey;
    private String name;
    private String description;
    private List<FieldDraftDto> fields = new ArrayList<>();

    public AiGeneratedSchemaDto() {}

    public AiGeneratedSchemaDto(String objectKey, String name, String description, List<FieldDraftDto> fields) {
        this.objectKey = objectKey;
        this.name = name;
        this.description = description;
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<FieldDraftDto> getFields() { return fields; }
    public void setFields(List<FieldDraftDto> fields) { this.fields = fields; }

    public static class FieldDraftDto {
        private String fieldKey;
        private String label;
        private String fieldType; // TEXT, NUMBER, DATE, CHOICE, BOOLEAN, REFERENCE
        private boolean required;
        private int displayOrder;
        private String defaultValue;
        private List<String> choiceOptions;

        public FieldDraftDto() {}

        public FieldDraftDto(String fieldKey, String label, String fieldType, boolean required, int displayOrder, String defaultValue, List<String> choiceOptions) {
            this.fieldKey = fieldKey;
            this.label = label;
            this.fieldType = fieldType;
            this.required = required;
            this.displayOrder = displayOrder;
            this.defaultValue = defaultValue;
            this.choiceOptions = choiceOptions;
        }

        public String getFieldKey() { return fieldKey; }
        public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

        public List<String> getChoiceOptions() { return choiceOptions; }
        public void setChoiceOptions(List<String> choiceOptions) { this.choiceOptions = choiceOptions; }
    }
}
