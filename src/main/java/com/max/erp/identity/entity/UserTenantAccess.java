package com.max.erp.identity.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_user_tenant_access")
public class UserTenantAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_security_user_tenant_access_user"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "tenant_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_security_user_tenant_access_tenant"
        )
    )
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        value = {
            @JoinColumn(
                name = "tenant_id",
                referencedColumnName = "tenant_id",
                insertable = false,
                updatable = false
            ),
            @JoinColumn(
                name = "organization_id",
                referencedColumnName = "id",
                insertable = false,
                updatable = false
            )
        }
    )
    private Organization organization;

    @Column(name = "organization_id")
    private UUID organizationId;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Organization getOrganization() {
        return organization;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
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