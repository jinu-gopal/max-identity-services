package com.max.erp.identity.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "configuration_module_entitlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_module_entitlements_tenant_module",
                        columnNames = {"tenant_id", "module_id"}
                )
        }
)
public class ModuleEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_module_entitlements_tenant"
            )
    )
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "module_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_module_entitlements_module"
            )
    )
    private Module module;

    @Column(nullable = false, length = 30)
    private String status = "ENABLED";

    @Column(name = "enabled_at", nullable = false)
    private Instant enabledAt;

    @Column(name = "disabled_at")
    private Instant disabledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (enabledAt == null) {
            enabledAt = now;
        }

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

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getEnabledAt() {
        return enabledAt;
    }

    public void setEnabledAt(Instant enabledAt) {
        this.enabledAt = enabledAt;
    }

    public Instant getDisabledAt() {
        return disabledAt;
    }

    public void setDisabledAt(Instant disabledAt) {
        this.disabledAt = disabledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}