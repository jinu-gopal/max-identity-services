package com.max.erp.identity.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "configuration_business_object_field_references",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_field_references_field",
                        columnNames = {"field_id"}
                )
        }
)
public class BusinessObjectFieldReference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "field_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_field_references_field"
            )
    )
    private BusinessObjectField field;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "target_business_object_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_field_references_target_object"
            )
    )
    private BusinessObject targetBusinessObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "display_field_id",
            foreignKey = @ForeignKey(
                    name = "fk_field_references_display_field"
            )
    )
    private BusinessObjectField displayField;

    @Column(name = "allow_multiple", nullable = false)
    private boolean allowMultiple = false;

    @Column(name = "allow_create", nullable = false)
    private boolean allowCreate = false;

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

    public BusinessObject getTargetBusinessObject() {
        return targetBusinessObject;
    }

    public void setTargetBusinessObject(BusinessObject targetBusinessObject) {
        this.targetBusinessObject = targetBusinessObject;
    }

    public BusinessObjectField getDisplayField() {
        return displayField;
    }

    public void setDisplayField(BusinessObjectField displayField) {
        this.displayField = displayField;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public boolean isAllowCreate() {
        return allowCreate;
    }

    public void setAllowCreate(boolean allowCreate) {
        this.allowCreate = allowCreate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}