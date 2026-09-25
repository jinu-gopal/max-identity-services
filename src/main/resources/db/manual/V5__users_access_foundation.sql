-- ============================================================
-- MAX ERP Identity Service
-- Manual Database Version V5
-- Users, Tenants, Organizations, Roles & Permissions
-- ============================================================

-- IMPORTANT:
-- security_permissions already exists from the previous
-- partial execution. This script intentionally preserves it.


-- ============================================================
-- 1. TENANTS
-- ============================================================

CREATE TABLE IF NOT EXISTS configuration_tenants (
    id UUID PRIMARY KEY,
    tenant_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_configuration_tenants_status
        CHECK (
            status IN (
                'ACTIVE',
                'SUSPENDED',
                'ARCHIVED'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_configuration_tenants_status
    ON configuration_tenants(status);


-- ============================================================
-- 2. ORGANIZATIONS
-- Supports:
-- COMPANY
-- DIVISION
-- BRANCH
-- DEPARTMENT
-- UNIT
-- LOCATION
-- ============================================================

CREATE TABLE IF NOT EXISTS configuration_organizations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    parent_organization_id UUID NULL,
    organization_key VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    organization_type VARCHAR(50) NOT NULL DEFAULT 'UNIT',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_configuration_organizations_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES configuration_tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_configuration_organizations_parent
        FOREIGN KEY (parent_organization_id)
        REFERENCES configuration_organizations(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_configuration_organizations_key
        UNIQUE (tenant_id, organization_key),

    CONSTRAINT uq_configuration_organizations_tenant_id
        UNIQUE (tenant_id, id),

    CONSTRAINT chk_configuration_organizations_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_configuration_organizations_type
        CHECK (
            organization_type IN (
                'COMPANY',
                'DIVISION',
                'BRANCH',
                'DEPARTMENT',
                'UNIT',
                'LOCATION'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_configuration_organizations_tenant
    ON configuration_organizations(tenant_id);

CREATE INDEX IF NOT EXISTS idx_configuration_organizations_parent
    ON configuration_organizations(parent_organization_id);

CREATE INDEX IF NOT EXISTS idx_configuration_organizations_status
    ON configuration_organizations(tenant_id, status);


-- ============================================================
-- 3. ROLES
--
-- tenant_id NULL:
--     MAX ERP platform/system role
--
-- tenant_id populated:
--     Customer-specific role
-- ============================================================

CREATE TABLE IF NOT EXISTS security_roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NULL,
    role_key VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_security_roles_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES configuration_tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_security_roles_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_security_roles_global_key
    ON security_roles(role_key)
    WHERE tenant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_security_roles_tenant_key
    ON security_roles(tenant_id, role_key)
    WHERE tenant_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_security_roles_tenant
    ON security_roles(tenant_id);

CREATE INDEX IF NOT EXISTS idx_security_roles_status
    ON security_roles(status);


-- ============================================================
-- 4. ROLE → PERMISSION
-- ============================================================

CREATE TABLE IF NOT EXISTS security_role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_security_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES security_roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_security_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES security_permissions(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_security_role_permissions_permission
    ON security_role_permissions(permission_id);


-- ============================================================
-- 5. USER → TENANT ACCESS
--
-- organization_id NULL:
--     User has access to the entire tenant.
--
-- organization_id populated:
--     User has access to that organization only.
--
-- A surrogate ID is used because PostgreSQL primary-key
-- columns cannot be nullable.
-- ============================================================

CREATE TABLE IF NOT EXISTS security_user_tenant_access (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    organization_id UUID NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_security_user_tenant_access_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_security_user_tenant_access_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES configuration_tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_security_user_tenant_access_organization
        FOREIGN KEY (tenant_id, organization_id)
        REFERENCES configuration_organizations(tenant_id, id)
        ON DELETE CASCADE,

    CONSTRAINT chk_security_user_tenant_access_status
        CHECK (
            status IN (
                'ACTIVE',
                'SUSPENDED',
                'REVOKED'
            )
        ),

    CONSTRAINT chk_security_user_tenant_access_scope
        CHECK (
            organization_id IS NULL
            OR tenant_id IS NOT NULL
        )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_security_user_tenant_access_scope
    ON security_user_tenant_access(
        user_id,
        tenant_id,
        COALESCE(
            organization_id,
            '00000000-0000-0000-0000-000000000000'::UUID
        )
    );

CREATE INDEX IF NOT EXISTS idx_security_user_tenant_access_user
    ON security_user_tenant_access(user_id);

CREATE INDEX IF NOT EXISTS idx_security_user_tenant_access_tenant
    ON security_user_tenant_access(tenant_id);

CREATE INDEX IF NOT EXISTS idx_security_user_tenant_access_organization
    ON security_user_tenant_access(organization_id);


-- ============================================================
-- 6. USER → ROLE
--
-- tenant_id NULL:
--     Global/system role assignment.
--
-- tenant_id populated + organization_id NULL:
--     Role applies across the tenant.
--
-- tenant_id populated + organization_id populated:
--     Role applies only to that organization.
-- ============================================================

CREATE TABLE IF NOT EXISTS security_user_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    tenant_id UUID NULL,
    organization_id UUID NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_security_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_security_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES security_roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_security_user_roles_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES configuration_tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_security_user_roles_organization
        FOREIGN KEY (tenant_id, organization_id)
        REFERENCES configuration_organizations(tenant_id, id)
        ON DELETE CASCADE,

    CONSTRAINT chk_security_user_roles_status
        CHECK (
            status IN (
                'ACTIVE',
                'SUSPENDED',
                'REVOKED'
            )
        ),

    CONSTRAINT chk_security_user_roles_scope
        CHECK (
            organization_id IS NULL
            OR tenant_id IS NOT NULL
        )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_security_user_roles_scope
    ON security_user_roles(
        user_id,
        role_id,
        COALESCE(
            tenant_id,
            '00000000-0000-0000-0000-000000000000'::UUID
        ),
        COALESCE(
            organization_id,
            '00000000-0000-0000-0000-000000000000'::UUID
        )
    );

CREATE INDEX IF NOT EXISTS idx_security_user_roles_user
    ON security_user_roles(user_id);

CREATE INDEX IF NOT EXISTS idx_security_user_roles_role
    ON security_user_roles(role_id);

CREATE INDEX IF NOT EXISTS idx_security_user_roles_tenant
    ON security_user_roles(tenant_id);

CREATE INDEX IF NOT EXISTS idx_security_user_roles_organization
    ON security_user_roles(organization_id);


-- ============================================================
-- END V5
-- ============================================================