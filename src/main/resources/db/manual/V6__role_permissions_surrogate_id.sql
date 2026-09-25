BEGIN;

ALTER TABLE security_role_permissions
ADD COLUMN IF NOT EXISTS id UUID;

UPDATE security_role_permissions
SET id = gen_random_uuid()
WHERE id IS NULL;

ALTER TABLE security_role_permissions
ALTER COLUMN id SET NOT NULL;

ALTER TABLE security_role_permissions
DROP CONSTRAINT IF EXISTS security_role_permissions_pkey;

ALTER TABLE security_role_permissions
ADD CONSTRAINT security_role_permissions_pkey
PRIMARY KEY (id);

CREATE UNIQUE INDEX IF NOT EXISTS
uq_security_role_permissions_role_permission
ON security_role_permissions(role_id, permission_id);

COMMIT;