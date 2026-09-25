package com.max.erp.identity.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "configuration_business_object_field_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_business_object_field_options_value",
                        columnNames = {"field_id", "option_value"}
                )
        }
)
public class BusinessObjectFieldOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "field_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_business_object_field_options_field"
            )
    )
    private BusinessObjectField field;

    @Column(name = "option_value", nullable = false, length = 200)
    private String optionValue;

    @Column(name = "option_label", nullable = false, length = 200)
    private String optionLabel;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "is_default", nullable = false)
    private boolean defaultOption = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public BusinessObjectField getField() {
        return field;
    }

    public void setField(BusinessObjectField field) {
        this.field = field;
    }

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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefaultOption() {
        return defaultOption;
    }

    public void setDefaultOption(boolean defaultOption) {
        this.defaultOption = defaultOption;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}