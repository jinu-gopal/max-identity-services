BEGIN;

-- ============================================================
-- MAX ERP SECURITY ADMIN BOOTSTRAP
-- ============================================================

-- ------------------------------------------------------------
-- 1. Security administration permissions
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
VALUES
(
    gen_random_uuid(),
    'SECURITY.ACCESS_MANAGE',
    'Manage Access',
    'Manage user tenant and organization access',
    'SECURITY',
    'ACCESS_MANAGE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'SECURITY.ROLE_MANAGE',
    'Manage Roles',
    'Create and manage security roles',
    'SECURITY',
    'ROLE_MANAGE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'SECURITY.ROLE_PERMISSION_MANAGE',
    'Manage Role Permissions',
    'Assign and remove permissions from roles',
    'SECURITY',
    'ROLE_PERMISSION_MANAGE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (permission_key) DO NOTHING;


-- ------------------------------------------------------------
-- 2. Add administration permissions to test manager role
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
    ON p.permission_key IN (
        'SECURITY.ACCESS_MANAGE',
        'SECURITY.ROLE_MANAGE',
        'SECURITY.ROLE_PERMISSION_MANAGE'
    )
WHERE t.tenant_key = 'security-test'
  AND r.role_key = 'security_test_manager'
  AND NOT EXISTS (
      SELECT 1
      FROM security_role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );


-- ------------------------------------------------------------
-- 3. Give admin tenant-wide access to test tenant
-- ------------------------------------------------------------

INSERT INTO security_user_tenant_access (
    id,
    user_id,
    tenant_id,
    organization_id,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    u.id,
    t.id,
    NULL,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
JOIN configuration_tenants t
    ON t.tenant_key = 'security-test'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM security_user_tenant_access uta
      WHERE uta.user_id = u.id
        AND uta.tenant_id = t.id
        AND uta.organization_id IS NULL
  );


-- ------------------------------------------------------------
-- 4. Give admin the security test manager role
--    at tenant scope.
-- ------------------------------------------------------------

INSERT INTO security_user_roles (
    id,
    user_id,
    role_id,
    tenant_id,
    organization_id,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    u.id,
    r.id,
    t.id,
    NULL,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
JOIN configuration_tenants t
    ON t.tenant_key = 'security-test'
JOIN security_roles r
    ON r.tenant_id = t.id
   AND r.role_key = 'security_test_manager'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM security_user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
        AND ur.tenant_id = t.id
        AND ur.organization_id IS NULL
  );


COMMIT;


-- ------------------------------------------------------------
-- Verification
-- ------------------------------------------------------------

SELECT
    u.username,
    t.tenant_key,
    uta.status AS tenant_access,
    r.role_key,
    ur.status AS role_status,
    p.permission_key,
    me.status AS module_entitlement
FROM users u
JOIN security_user_tenant_access uta
    ON uta.user_id = u.id
JOIN configuration_tenants t
    ON t.id = uta.tenant_id
JOIN security_user_roles ur
    ON ur.user_id = u.id
   AND ur.tenant_id = t.id
JOIN security_roles r
    ON r.id = ur.role_id
JOIN security_role_permissions rp
    ON rp.role_id = r.id
JOIN security_permissions p
    ON p.id = rp.permission_id
JOIN configuration_modules m
    ON m.module_key = p.module_key
JOIN configuration_module_entitlements me
    ON me.tenant_id = t.id
   AND me.module_id = m.id
WHERE u.username = 'admin'
  AND t.tenant_key = 'security-test'
ORDER BY p.permission_key;
