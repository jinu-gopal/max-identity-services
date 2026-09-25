BEGIN;

ALTER TABLE security_role_permissions
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

UPDATE security_role_permissions
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE security_role_permissions
ALTER COLUMN updated_at SET NOT NULL;

COMMIT;