package com.max.erp.identity.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "configuration_organizations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_configuration_organizations_key",
            columnNames = {"tenant_id", "organization_key"}
        )
    }
)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "tenant_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_configuration_organizations_tenant"
        )
    )
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "parent_organization_id",
        foreignKey = @ForeignKey(
            name = "fk_configuration_organizations_parent"
        )
    )
    private Organization parentOrganization;

    @Column(name = "organization_key", nullable = false, length = 100)
    private String organizationKey;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "organization_type", nullable = false, length = 50)
    private String organizationType = "UNIT";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Organization getParentOrganization() {
        return parentOrganization;
    }

    public void setParentOrganization(
            Organization parentOrganization) {
        this.parentOrganization = parentOrganization;
    }

    public String getOrganizationKey() {
        return organizationKey;
    }

    public void setOrganizationKey(String organizationKey) {
        this.organizationKey = organizationKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
