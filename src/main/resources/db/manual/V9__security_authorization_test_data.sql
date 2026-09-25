BEGIN;

-- ============================================================
-- MAX ERP SECURITY TEST DATA
-- Purpose:
--   Controlled local verification of:
--   User -> Tenant -> Organization -> Role
--   -> Permission -> Module -> Entitlement
--
-- This script does NOT create passwords or users.
-- It uses the existing admin user.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Create test tenant
-- ------------------------------------------------------------

INSERT INTO configuration_tenants (
    id,
    tenant_key,
    name,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'security-test',
    'MAX Security Test Tenant',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM configuration_tenants
    WHERE tenant_key = 'security-test'
);


-- ------------------------------------------------------------
-- 2. Create organizations
-- ------------------------------------------------------------

INSERT INTO configuration_organizations (
    id,
    tenant_id,
    parent_organization_id,
    organization_key,
    name,
    organization_type,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    t.id,
    NULL,
    'security-test-hospital',
    'Security Test Hospital',
    'DIVISION',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM configuration_tenants t
WHERE t.tenant_key = 'security-test'
AND NOT EXISTS (
    SELECT 1
    FROM configuration_organizations o
    WHERE o.organization_key = 'security-test-hospital'
);


INSERT INTO configuration_organizations (
    id,
    tenant_id,
    parent_organization_id,
    organization_key,
    name,
    organization_type,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    t.id,
    NULL,
    'security-test-finance',
    'Security Test Finance',
    'DEPARTMENT',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM configuration_tenants t
WHERE t.tenant_key = 'security-test'
AND NOT EXISTS (
    SELECT 1
    FROM configuration_organizations o
    WHERE o.organization_key = 'security-test-finance'
);


-- ------------------------------------------------------------
-- 3. Create SECURITY test module
-- ------------------------------------------------------------

INSERT INTO configuration_modules (
    id,
    module_key,
    name,
    description,
    version_number,
    status,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'SECURITY',
    'Security',
    'MAX ERP security and access management',
    1,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (module_key) DO NOTHING;


-- ------------------------------------------------------------
-- 4. Create test permission
-- ------------------------------------------------------------

INSERT INTO security_permissions (
    id,
    permission_key,
    name,
    description,
    module_key,
    action_key,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'SECURITY.USER_ROLE_ASSIGN',
    'Assign User Role',
    'Allows assigning a role to a user within an authorized tenant scope',
    'SECURITY',
    'USER_ROLE_ASSIGN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM security_permissions
    WHERE permission_key = 'SECURITY.USER_ROLE_ASSIGN'
);


-- ------------------------------------------------------------
-- 5. Create test role
-- ------------------------------------------------------------

INSERT INTO security_roles (
    id,
    tenant_id,
    role_key,
    name,
    description,
    is_system,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    t.id,
    'security_test_manager',
    'Security Test Manager',
    'Role used to verify MAX ERP authorization',
    FALSE,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM configuration_tenants t
WHERE t.tenant_key = 'security-test'
AND NOT EXISTS (
    SELECT 1
    FROM security_roles r
    WHERE r.tenant_id = t.id
      AND r.role_key = 'security_test_manager'
);


-- ------------------------------------------------------------
-- 6. Assign permission to role
-- ------------------------------------------------------------

INSERT INTO security_role_permissions (
    id,
    role_id,
    permission_id,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM security_roles r
JOIN configuration_tenants t
    ON t.id = r.tenant_id
JOIN security_permissions p
    ON p.permission_key = 'SECURITY.USER_ROLE_ASSIGN'
WHERE t.tenant_key = 'security-test'
  AND r.role_key = 'security_test_manager'
  AND NOT EXISTS (
      SELECT 1
      FROM security_role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );


-- ------------------------------------------------------------
-- 7. Enable SECURITY module for test tenant
-- ------------------------------------------------------------

INSERT INTO configuration_module_entitlements (
    id,
    tenant_id,
    module_id,
    status,
    enabled_at,
    disabled_at,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    t.id,
    m.id,
    'ENABLED',
    CURRENT_TIMESTAMP,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM configuration_tenants t
JOIN configuration_modules m
    ON m.module_key = 'SECURITY'
WHERE t.tenant_key = 'security-test'
AND NOT EXISTS (
    SELECT 1
    FROM configuration_module_entitlements me
    WHERE me.tenant_id = t.id
      AND me.module_id = m.id
  );


COMMIT;

-- ------------------------------------------------------------
-- Verification
-- ------------------------------------------------------------

SELECT
    t.tenant_key,
    t.name AS tenant_name,
    o.organization_key,
    o.name AS organization_name,
    r.role_key,
    p.permission_key,
    m.module_key,
    me.status AS module_entitlement
FROM configuration_tenants t
LEFT JOIN configuration_organizations o
    ON o.tenant_id = t.id
LEFT JOIN security_roles r
    ON r.tenant_id = t.id
LEFT JOIN security_role_permissions rp
    ON rp.role_id = r.id
LEFT JOIN security_permissions p
    ON p.id = rp.permission_id
LEFT JOIN configuration_modules m
    ON m.module_key = p.module_key
LEFT JOIN configuration_module_entitlements me
    ON me.tenant_id = t.id
   AND me.module_id = m.id
WHERE t.tenant_key = 'security-test'
ORDER BY
    o.organization_key,
    r.role_key,
    p.permission_key;
