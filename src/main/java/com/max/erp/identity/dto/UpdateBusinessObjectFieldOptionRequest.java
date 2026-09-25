package com.max.erp.identity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateBusinessObjectFieldOptionRequest {

    @Size(max = 200, message = "Option value cannot exceed 200 characters")
    private String optionValue;

    @Size(max = 200, message = "Option label cannot exceed 200 characters")
    private String optionLabel;

    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;

    private Boolean active;

    private Boolean defaultOption;

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDefaultOption() {
        return defaultOption;
    }

    public void setDefaultOption(Boolean defaultOption) {
        this.defaultOption = defaultOption;
    }
}