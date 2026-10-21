-- ============================================================================
-- MAX ERP Database Rollback Script: U14__tenant_isolation_shared_objects.sql
-- Description:
--   Rollback for V14__tenant_isolation_shared_objects.sql:
--   - Enforces strict pre-traffic rollback boundaries.
--   - Protects against data loss: Refuses rollback once tenant custom fields,
--     post-migration records/jobs, or unexpected dependents exist.
--   - Removes ONLY objects specifically owned by V14 in system_migration_manifest.
--   - Restores the original uq_business_object_fields_key unique constraint.
--   - Adheres to the Permanent Safety Policy (Non-destructive column retention).
--
-- Execution Model: Single-Session, Single-Transaction Atomic Execution
-- Invocation: psql -v ON_ERROR_STOP=1 -X -q -f U14__tenant_isolation_shared_objects.sql
-- Note: Do NOT execute without explicit phase authorization.
-- ============================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ============================================================================
-- STEP 1: PRE-ROLLBACK SAFEGUARDS & DEPENDENCY CHECKS
-- ============================================================================

-- 1.1 Acquire Advisory Lock
SELECT pg_advisory_xact_lock(hashtext('MAX_ERP_V14_MIGRATION'));

-- 1.2 Verify V14 Ownership Ledger and Application Status
DO $$
DECLARE
    v_v14_entries_cnt INTEGER;
    v_custom_fields   INTEGER;
    v_records_cnt     INTEGER;
    v_jobs_cnt        INTEGER;
    v_rows_cnt        INTEGER;
    v_ext_perms       INTEGER;
    v_ext_roles       INTEGER;
    v_ext_entitlements INTEGER;
BEGIN
    -- Check manifest exists and V14 is recorded
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'system_migration_manifest') THEN
        RAISE EXCEPTION 'Rollback Refused: system_migration_manifest does not exist. V14 ownership cannot be proven.';
    END IF;

    SELECT count(*) INTO v_v14_entries_cnt 
    FROM system_migration_manifest 
    WHERE migration_version = 'V14';

    IF v_v14_entries_cnt = 0 THEN
        RAISE EXCEPTION 'Rollback Refused: Migration V14 is not recorded as applied in system_migration_manifest.';
    END IF;

    -- Safety Check 1: Refuse if Tenant Custom Fields Exist
    SELECT count(*) INTO v_custom_fields 
    FROM configuration_business_object_fields 
    WHERE tenant_id IS NOT NULL;

    IF v_custom_fields > 0 THEN
        RAISE EXCEPTION 'Rollback Refused: % tenant custom fields exist. Structural rollback would cause irreversible data loss. Decommission tenant custom fields explicitly first.', v_custom_fields;
    END IF;

    -- Safety Check 2: Refuse if Post-Migration Writes or Record Drift Exist
    SELECT count(*) INTO v_records_cnt FROM configuration_business_object_records;
    SELECT count(*) INTO v_jobs_cnt FROM configuration_import_jobs;
    SELECT count(*) INTO v_rows_cnt FROM configuration_import_rows;

    IF v_records_cnt != 10 OR v_jobs_cnt != 5 OR v_rows_cnt != 11 THEN
        RAISE EXCEPTION 'Rollback Refused: Record or import job counts differ from pre-migration baseline (found % records, % jobs, % rows). Post-traffic rollback prohibited.', v_records_cnt, v_jobs_cnt, v_rows_cnt;
    END IF;

    -- Safety Check 3: Refuse if External Roles Were Assigned CONFIGURATION Permissions
    SELECT count(*) INTO v_ext_roles
    FROM security_role_permissions
    WHERE permission_id IN (
        SELECT entity_id FROM system_migration_manifest 
        WHERE migration_version = 'V14' AND entity_table = 'security_permissions'
    )
    AND id NOT IN (
        SELECT entity_id FROM system_migration_manifest 
        WHERE migration_version = 'V14' AND entity_table = 'security_role_permissions'
    );

    IF v_ext_roles > 0 THEN
        RAISE EXCEPTION 'Rollback Refused: % external role assignments exist for CONFIGURATION permissions. Reassign or revoke them first.', v_ext_roles;
    END IF;

    -- Safety Check 4: Refuse if Non-V14 Permissions Exist Under CONFIGURATION Module
    SELECT count(*) INTO v_ext_perms
    FROM security_permissions
    WHERE module_key = 'CONFIGURATION'
      AND id NOT IN (
          SELECT entity_id FROM system_migration_manifest 
          WHERE migration_version = 'V14' AND entity_table = 'security_permissions'
      );

    IF v_ext_perms > 0 THEN
        RAISE EXCEPTION 'Rollback Refused: % non-V14 permissions exist under CONFIGURATION module. Remove them first.', v_ext_perms;
    END IF;

    -- Safety Check 5: Refuse if Other Tenants Are Entitled to CONFIGURATION Module
    SELECT count(*) INTO v_ext_entitlements
    FROM configuration_module_entitlements
    WHERE module_id = 'c0000014-0000-0000-0000-000000000001'
      AND id NOT IN (
          SELECT entity_id FROM system_migration_manifest 
          WHERE migration_version = 'V14' AND entity_table = 'configuration_module_entitlements'
      );

    IF v_ext_entitlements > 0 THEN
        RAISE EXCEPTION 'Rollback Refused: % other tenant entitlements exist for CONFIGURATION module.', v_ext_entitlements;
    END IF;

    RAISE NOTICE 'ALL ROLLBACK PRE-FLIGHT GUARDS PASSED CLEANLY';
END $$;


-- ============================================================================
-- STEP 2: REMOVE TRIGGER, PARTIAL INDEXES & RESTORE ORIGINAL UNIQUE CONSTRAINT
-- ============================================================================

-- 2.1 Drop Collision Trigger and Trigger Function
DROP TRIGGER IF EXISTS trg_check_field_key_collision ON configuration_business_object_fields;
DROP FUNCTION IF EXISTS fn_check_field_key_collision();

-- 2.2 Drop Partial Unique Indexes
DROP INDEX IF EXISTS uq_fields_global_key;
DROP INDEX IF EXISTS uq_fields_tenant_key;

-- 2.3 Restore Original Global Unique Constraint
ALTER TABLE configuration_business_object_fields
    ADD CONSTRAINT uq_business_object_fields_key 
        UNIQUE (business_object_id, field_key);


-- ============================================================================
-- STEP 3: REVERT TENANT FOREIGN KEYS AND BACKFILLED COLUMNS (NON-DESTRUCTIVE)
-- ============================================================================

-- 3.1 Drop Added Foreign Key Constraints
ALTER TABLE configuration_business_object_records
    DROP CONSTRAINT IF EXISTS fk_records_tenant;

ALTER TABLE configuration_import_jobs
    DROP CONSTRAINT IF EXISTS fk_import_jobs_tenant;

ALTER TABLE configuration_business_object_fields
    DROP CONSTRAINT IF EXISTS fk_fields_tenant;

-- 3.2 Revert NOT NULL and Reset Backfilled tenant_id to NULL
ALTER TABLE configuration_business_object_records
    ALTER COLUMN tenant_id DROP NOT NULL;

UPDATE configuration_business_object_records
    SET tenant_id = NULL;

ALTER TABLE configuration_import_jobs
    ALTER COLUMN tenant_id DROP NOT NULL;

UPDATE configuration_import_jobs
    SET tenant_id = NULL;

-- Note on Permanent Safety Policy:
-- Under the strict no-deletion policy, columns configuration_business_object_fields.tenant_id,
-- configuration_business_object_records.tenant_id, and configuration_import_jobs.tenant_id
-- are left nullable with NULL values rather than executing destructive DROP COLUMN statements.


-- ============================================================================
-- STEP 4: REMOVE SECURITY ENTITIES OWNED STRICTLY BY V14
-- ============================================================================

-- 4.1 Delete Owned Role Permissions
DELETE FROM security_role_permissions
WHERE id IN (
    SELECT entity_id FROM system_migration_manifest 
    WHERE migration_version = 'V14' AND entity_table = 'security_role_permissions'
);

-- 4.2 Delete Owned Permissions
DELETE FROM security_permissions
WHERE id IN (
    SELECT entity_id FROM system_migration_manifest 
    WHERE migration_version = 'V14' AND entity_table = 'security_permissions'
);

-- 4.3 Delete Owned Entitlements
DELETE FROM configuration_module_entitlements
WHERE id IN (
    SELECT entity_id FROM system_migration_manifest 
    WHERE migration_version = 'V14' AND entity_table = 'configuration_module_entitlements'
);

-- 4.4 Delete Owned Modules
DELETE FROM configuration_modules
WHERE id IN (
    SELECT entity_id FROM system_migration_manifest 
    WHERE migration_version = 'V14' AND entity_table = 'configuration_modules'
);

-- 4.5 Clean Up V14 Ownership Ledger Entries
DELETE FROM system_migration_manifest
WHERE migration_version = 'V14';


-- ============================================================================
-- STEP 5: POST-ROLLBACK VERIFICATION ASSERTIONS
-- ============================================================================

DO $$
DECLARE
    v_ref_cnt INTEGER;
    v_perm_cnt INTEGER;
    v_mod_cnt INTEGER;
    v_uq_cnt INTEGER;
BEGIN
    -- Verify Original Unique Constraint Restored
    SELECT count(*) INTO v_uq_cnt 
    FROM pg_constraint 
    WHERE conname = 'uq_business_object_fields_key' 
      AND conrelid = 'configuration_business_object_fields'::regclass;

    IF v_uq_cnt != 1 THEN
        RAISE EXCEPTION 'Post-Rollback Failure: Constraint uq_business_object_fields_key not restored';
    END IF;

    -- Verify Absence of CONFIGURATION Security Artifacts
    SELECT count(*) INTO v_perm_cnt FROM security_permissions WHERE module_key = 'CONFIGURATION';
    SELECT count(*) INTO v_mod_cnt FROM configuration_modules WHERE module_key = 'CONFIGURATION';

    IF v_perm_cnt != 0 OR v_mod_cnt != 0 THEN
        RAISE EXCEPTION 'Post-Rollback Failure: CONFIGURATION security artifacts still exist';
    END IF;

    -- Verify Orphaned Reference Row Remains Intact
    SELECT count(*) INTO v_ref_cnt 
    FROM configuration_business_object_field_references
    WHERE id = '02b81e12-b74d-4684-9958-e2d0a7ff7c9b'
      AND field_id = 'df8ddb46-096b-499d-a708-0ad57d5c503f'
      AND target_business_object_id = '84d3442c-3fd7-4f92-8ab2-fde10ae8c85b'
      AND display_field_id = 'd9a04ba6-2093-4b6d-a78d-234c1e32a8e8'
      AND allow_multiple = FALSE
      AND allow_create = TRUE;

    IF v_ref_cnt != 1 THEN
        RAISE EXCEPTION 'Post-Rollback Failure: Orphaned reference row missing or altered';
    END IF;

    RAISE NOTICE 'ALL POST-ROLLBACK ASSERTIONS VERIFIED CLEANLY';
END $$;

COMMIT;
