BEGIN;

CREATE TABLE IF NOT EXISTS configuration_modules (
    id UUID PRIMARY KEY,
    module_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    version_number INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_configuration_modules_version
        CHECK (version_number > 0),

    CONSTRAINT chk_configuration_modules_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'DEPRECATED'
            )
        )
);

CREATE INDEX IF NOT EXISTS
idx_configuration_modules_status
ON configuration_modules(status);


CREATE TABLE IF NOT EXISTS configuration_module_entitlements (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    module_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ENABLED',
    enabled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disabled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_module_entitlements_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES configuration_tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_module_entitlements_module
        FOREIGN KEY (module_id)
        REFERENCES configuration_modules(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_module_entitlements_tenant_module
        UNIQUE (tenant_id, module_id),

    CONSTRAINT chk_module_entitlements_status
        CHECK (
            status IN (
                'ENABLED',
                'DISABLED'
            )
        )
);

CREATE INDEX IF NOT EXISTS
idx_module_entitlements_tenant
ON configuration_module_entitlements(tenant_id);

CREATE INDEX IF NOT EXISTS
idx_module_entitlements_module
ON configuration_module_entitlements(module_id);

CREATE INDEX IF NOT EXISTS
idx_module_entitlements_tenant_status
ON configuration_module_entitlements(
    tenant_id,
    status
);

COMMIT;