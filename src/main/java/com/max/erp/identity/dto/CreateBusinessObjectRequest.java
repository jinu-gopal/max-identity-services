package com.max.erp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateBusinessObjectRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(
            regexp = "^[a-z][a-z0-9_]*$",
            message = "objectKey must start with a lowercase letter and contain only lowercase letters, numbers, and underscores"
    )
    private String objectKey;

    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}