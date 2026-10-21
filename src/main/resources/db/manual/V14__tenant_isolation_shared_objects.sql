-- ============================================================================
-- MAX ERP Database Migration: V14__tenant_isolation_shared_objects.sql
-- Description:
--   Tenant Isolation for Shared Business Objects:
--   - Scopes business object records and import jobs to tenants (tenant_id NOT NULL)
--   - Adds nullable tenant_id to business object fields (NULL = global, NOT NULL = custom)
--   - Enforces field key uniqueness (partial unique indexes + cross-scope collision trigger)
--   - Backfills existing development test data to tenant security-test (cf1757d5-...)
--   - Provisions CONFIGURATION module, entitlement, permissions, and role assignments
--   - Tracks created objects in system_migration_manifest
--
-- Execution Model: Single-Session, Single-Transaction Atomic Execution
-- Invocation: psql -v ON_ERROR_STOP=1 -X -q -f V14__tenant_isolation_shared_objects.sql
-- Note: Do NOT execute without explicit phase authorization.
-- ============================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ============================================================================
-- STEP 1: CONCURRENCY MUTUAL EXCLUSION & PREFLIGHT VALIDATION
-- ============================================================================

-- 1.1 Acquire Transaction-Level Advisory Lock to Prevent Concurrent Execution
SELECT pg_advisory_xact_lock(hashtext('MAX_ERP_V14_MIGRATION'));

-- 1.2 Validate or Provision Migration Manifest Table (Ownership Ledger)
DO $$
DECLARE
    v_tbl_exists BOOLEAN;
    v_col_count INTEGER;
    v_pk_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'system_migration_manifest'
    ) INTO v_tbl_exists;

    IF NOT v_tbl_exists THEN
        CREATE TABLE system_migration_manifest (
            migration_version VARCHAR(20) NOT NULL,
            entity_table      VARCHAR(100) NOT NULL,
            entity_id         UUID NOT NULL,
            created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (migration_version, entity_table, entity_id)
        );
    ELSE
        -- Validate schema compatibility
        SELECT count(*) INTO v_col_count
        FROM information_schema.columns
        WHERE table_schema = 'public' 
          AND table_name = 'system_migration_manifest'
          AND (
              (column_name = 'migration_version' AND data_type = 'character varying') OR
              (column_name = 'entity_table' AND data_type = 'character varying') OR
              (column_name = 'entity_id' AND data_type = 'uuid') OR
              (column_name = 'created_at' AND data_type LIKE 'timestamp%')
          );
        IF v_col_count != 4 THEN
            RAISE EXCEPTION 'Preflight Failure: system_migration_manifest exists with incompatible columns';
        END IF;

        -- Validate Primary Key constraint
        SELECT EXISTS (
            SELECT 1 FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu 
              ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
            WHERE tc.table_schema = 'public' 
              AND tc.table_name = 'system_migration_manifest'
              AND tc.constraint_type = 'PRIMARY KEY'
            GROUP BY tc.constraint_name
            HAVING count(*) = 3
               AND bool_and(kcu.column_name IN ('migration_version', 'entity_table', 'entity_id'))
        ) INTO v_pk_exists;
        IF NOT v_pk_exists THEN
            RAISE EXCEPTION 'Preflight Failure: system_migration_manifest PK constraint missing or invalid';
        END IF;

        -- Check for prior completed execution
        IF EXISTS (SELECT 1 FROM system_migration_manifest WHERE migration_version = 'V14') THEN
            RAISE EXCEPTION 'Preflight Failure: Migration V14 is already recorded in system_migration_manifest';
        END IF;
    END IF;
END $$;

-- 1.3 Capture Baseline Snapshot of Orphaned Reference Row in Session Temp Table
CREATE TEMP TABLE _v14_ref_baseline ON COMMIT DROP AS
SELECT * FROM configuration_business_object_field_references
WHERE id = '02b81e12-b74d-4684-9958-e2d0a7ff7c9b';

-- 1.4 Preflight Comprehensive Assertions
DO $$
DECLARE
    v_tenant_cnt       INTEGER;
    v_role_cnt         INTEGER;
    v_records_cnt      INTEGER;
    v_jobs_cnt         INTEGER;
    v_fields_cnt       INTEGER;
    v_options_cnt      INTEGER;
    v_ref_cnt          INTEGER;
    v_rows_cnt         INTEGER;
    v_diff_cnt         INTEGER;
    v_orphaned_cnt     INTEGER;
    v_non_obj_records  INTEGER;
    v_unmapped_records INTEGER;
    v_unmapped_jobs    INTEGER;
    v_mod              RECORD;
BEGIN
    -- Assert Authoritative Active Tenant 'security-test'
    SELECT count(*) INTO v_tenant_cnt 
    FROM configuration_tenants 
    WHERE id = 'cf1757d5-c535-4335-939c-24908aee0209' AND status = 'ACTIVE';
    IF v_tenant_cnt != 1 THEN 
        RAISE EXCEPTION 'Preflight Failure: Authoritative tenant security-test missing or inactive'; 
    END IF;

    -- Assert Authoritative Active Role 'security_test_manager'
    SELECT count(*) INTO v_role_cnt 
    FROM security_roles 
    WHERE id = '89b34568-f484-48ed-958e-177a3ca688ae' 
      AND role_key = 'security_test_manager' 
      AND tenant_id = 'cf1757d5-c535-4335-939c-24908aee0209' 
      AND status = 'ACTIVE';
    IF v_role_cnt != 1 THEN 
        RAISE EXCEPTION 'Preflight Failure: Role security_test_manager missing or inactive'; 
    END IF;

    -- Assert Baseline Table Counts
    SELECT count(*) INTO v_records_cnt FROM configuration_business_object_records;
    SELECT count(*) INTO v_jobs_cnt FROM configuration_import_jobs;
    SELECT count(*) INTO v_fields_cnt FROM configuration_business_object_fields;
    SELECT count(*) INTO v_options_cnt FROM configuration_business_object_field_options;
    SELECT count(*) INTO v_ref_cnt FROM configuration_business_object_field_references;
    SELECT count(*) INTO v_rows_cnt FROM configuration_import_rows;

    IF v_records_cnt != 10 THEN RAISE EXCEPTION 'Preflight Failure: Expected 10 records, found %', v_records_cnt; END IF;
    IF v_jobs_cnt != 5 THEN RAISE EXCEPTION 'Preflight Failure: Expected 5 import jobs, found %', v_jobs_cnt; END IF;
    IF v_fields_cnt != 16 THEN RAISE EXCEPTION 'Preflight Failure: Expected 16 fields, found %', v_fields_cnt; END IF;
    IF v_options_cnt != 4 THEN RAISE EXCEPTION 'Preflight Failure: Expected 4 options, found %', v_options_cnt; END IF;
    IF v_ref_cnt != 1 THEN RAISE EXCEPTION 'Preflight Failure: Expected 1 reference, found %', v_ref_cnt; END IF;
    IF v_rows_cnt != 11 THEN RAISE EXCEPTION 'Preflight Failure: Expected 11 import rows, found %', v_rows_cnt; END IF;

    -- Assert Record JSON Data Integrity
    SELECT count(*) INTO v_non_obj_records 
    FROM configuration_business_object_records 
    WHERE record_data IS NULL OR jsonb_typeof(record_data) != 'object';
    IF v_non_obj_records != 0 THEN 
        RAISE EXCEPTION 'Preflight Failure: Found % records with null or non-object record_data', v_non_obj_records; 
    END IF;

    -- Assert Explicit Record ID Set
    SELECT count(*) INTO v_unmapped_records
    FROM configuration_business_object_records
    WHERE id NOT IN (
        '428bf126-2e9a-4d02-9ad8-8c289b59d8fc', 'cfeeb4ec-133a-4a0d-a796-a093793dbc25',
        '0c708f4b-c685-4c40-a359-4865bc309c52', 'da41bc6b-57e8-444e-933d-c255c38c1849',
        '1709dfce-8968-4ea6-94cd-3f722e76b7df', '0674369d-67a6-43db-b665-4ea3a1fb4bd4',
        '59f86d7f-6fd6-4634-9bc6-7e48428bb1d4', '88158967-01cc-4d73-83c8-35adde3332af',
        '6bf83f95-9484-41e0-be81-a864ae976d1c', 'b09ae2f1-21a4-4591-b3c4-2f8a5907bd94'
    );
    IF v_unmapped_records != 0 THEN
        RAISE EXCEPTION 'Preflight Failure: Unexpected record IDs detected in database';
    END IF;

    -- Assert Bidirectional Reconciliation of Import Jobs and Child Rows
    WITH expected_jobs(job_id, expected_cnt) AS (
        VALUES 
            ('9df35470-d076-4f7f-ba67-74ffd15db10b'::uuid, 1),
            ('23e0cb3a-743c-427a-b1eb-0c0699720eba'::uuid, 2),
            ('72082d3b-fb3c-44d4-bdf5-8d96e4982b32'::uuid, 2),
            ('e4edd27c-183f-4e3c-a51c-788f1ecc70b0'::uuid, 3),
            ('6ffecf74-2e65-45de-bfd8-738742c17dae'::uuid, 3)
    ),
    actual_jobs AS (
        SELECT j.id AS job_id, count(r.id)::int AS actual_cnt
        FROM configuration_import_jobs j
        LEFT JOIN configuration_import_rows r ON r.import_job_id = j.id
        GROUP BY j.id
    ),
    diff AS (
        SELECT COALESCE(e.job_id, a.job_id) AS job_id, e.expected_cnt, a.actual_cnt
        FROM expected_jobs e
        FULL OUTER JOIN actual_jobs a ON e.job_id = a.job_id
        WHERE e.job_id IS NULL OR a.job_id IS NULL OR e.expected_cnt != a.actual_cnt
    )
    SELECT count(*) INTO v_diff_cnt FROM diff;

    IF v_diff_cnt != 0 THEN
        RAISE EXCEPTION 'Preflight Failure: Bidirectional import job reconciliation failed (% differences)', v_diff_cnt;
    END IF;

    SELECT count(*) INTO v_orphaned_cnt 
    FROM configuration_import_rows 
    WHERE import_job_id NOT IN (
        '9df35470-d076-4f7f-ba67-74ffd15db10b'::uuid, '23e0cb3a-743c-427a-b1eb-0c0699720eba'::uuid,
        '72082d3b-fb3c-44d4-bdf5-8d96e4982b32'::uuid, 'e4edd27c-183f-4e3c-a51c-788f1ecc70b0'::uuid,
        '6ffecf74-2e65-45de-bfd8-738742c17dae'::uuid
    );
    IF v_orphaned_cnt != 0 THEN
        RAISE EXCEPTION 'Preflight Failure: Orphaned import rows detected';
    END IF;

    -- Assert Orphaned Reference Row Baseline Capture in Temp Table
    SELECT count(*) INTO v_ref_cnt FROM _v14_ref_baseline
    WHERE id = '02b81e12-b74d-4684-9958-e2d0a7ff7c9b'
      AND field_id = 'df8ddb46-096b-499d-a708-0ad57d5c503f'
      AND target_business_object_id = '84d3442c-3fd7-4f92-8ab2-fde10ae8c85b'
      AND display_field_id = 'd9a04ba6-2093-4b6d-a78d-234c1e32a8e8'
      AND allow_multiple = FALSE
      AND allow_create = TRUE;
    IF v_ref_cnt != 1 THEN 
        RAISE EXCEPTION 'Preflight Failure: Baseline reference snapshot capture failed'; 
    END IF;

    -- Preflight Case Policy for CONFIGURATION Module
    SELECT * INTO v_mod FROM configuration_modules WHERE module_key = 'CONFIGURATION';
    IF FOUND THEN
        IF v_mod.id != 'c0000014-0000-0000-0000-000000000001' OR v_mod.status != 'ACTIVE' THEN
            RAISE EXCEPTION 'Preflight Failure: Module CONFIGURATION exists with conflicting ID or status';
        END IF;
    END IF;

    -- Assert Trigger and Trigger Function Do Not Pre-exist
    IF EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'fn_check_field_key_collision') THEN
        RAISE EXCEPTION 'Preflight Failure: Function fn_check_field_key_collision already exists';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_check_field_key_collision') THEN
        RAISE EXCEPTION 'Preflight Failure: Trigger trg_check_field_key_collision already exists';
    END IF;

    RAISE NOTICE 'ALL PREFLIGHT ASSERTIONS VERIFIED CLEANLY';
END $$;


-- ============================================================================
-- STEP 2: SCHEMA ALTERATIONS & APPROVED TENANT BACKFILL
-- ============================================================================

-- 2.1 Add tenant_id Columns
ALTER TABLE configuration_business_object_fields
    ADD COLUMN tenant_id UUID NULL,
    ADD CONSTRAINT fk_fields_tenant 
        FOREIGN KEY (tenant_id) REFERENCES configuration_tenants(id) ON DELETE RESTRICT;

ALTER TABLE configuration_business_object_records
    ADD COLUMN tenant_id UUID NULL;

ALTER TABLE configuration_import_jobs
    ADD COLUMN tenant_id UUID NULL;

-- 2.2 Execute Approved Backfill to Authoritative Tenant 'security-test'
UPDATE configuration_business_object_records
    SET tenant_id = 'cf1757d5-c535-4335-939c-24908aee0209'
    WHERE tenant_id IS NULL;

UPDATE configuration_import_jobs
    SET tenant_id = 'cf1757d5-c535-4335-939c-24908aee0209'
    WHERE tenant_id IS NULL;

-- 2.3 Enforce NOT NULL Constraints and Foreign Keys with RESTRICT
ALTER TABLE configuration_business_object_records
    ALTER COLUMN tenant_id SET NOT NULL,
    ADD CONSTRAINT fk_records_tenant 
        FOREIGN KEY (tenant_id) REFERENCES configuration_tenants(id) ON DELETE RESTRICT;

ALTER TABLE configuration_import_jobs
    ALTER COLUMN tenant_id SET NOT NULL,
    ADD CONSTRAINT fk_import_jobs_tenant 
        FOREIGN KEY (tenant_id) REFERENCES configuration_tenants(id) ON DELETE RESTRICT;

-- 2.4 Replace Global Unconditional Unique Constraint with Partial Unique Indexes
ALTER TABLE configuration_business_object_fields
    DROP CONSTRAINT uq_business_object_fields_key;

CREATE UNIQUE INDEX uq_fields_global_key 
    ON configuration_business_object_fields (business_object_id, field_key) 
    WHERE (tenant_id IS NULL);

CREATE UNIQUE INDEX uq_fields_tenant_key 
    ON configuration_business_object_fields (business_object_id, tenant_id, field_key) 
    WHERE (tenant_id IS NOT NULL);

-- 2.5 Create Concurrency-Safe Cross-Scope Collision Trigger Function
CREATE FUNCTION fn_check_field_key_collision()
RETURNS TRIGGER AS $$
BEGIN
    -- Acquire row locks on parent business objects in canonical UUID order to serialize mutations
    IF TG_OP = 'UPDATE' AND OLD.business_object_id != NEW.business_object_id THEN
        PERFORM id FROM configuration_business_objects 
        WHERE id = LEAST(OLD.business_object_id, NEW.business_object_id) 
        FOR NO KEY UPDATE;

        PERFORM id FROM configuration_business_objects 
        WHERE id = GREATEST(OLD.business_object_id, NEW.business_object_id) 
        FOR NO KEY UPDATE;
    ELSE
        PERFORM id FROM configuration_business_objects 
        WHERE id = NEW.business_object_id 
        FOR NO KEY UPDATE;
    END IF;

    -- Cross-scope collision check
    IF NEW.tenant_id IS NOT NULL THEN
        -- Case A: Tenant Custom Field cannot collide with a global field key on the same business object
        IF EXISTS (
            SELECT 1 FROM configuration_business_object_fields
            WHERE business_object_id = NEW.business_object_id
              AND tenant_id IS NULL
              AND field_key = NEW.field_key
              AND id != COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid)
        ) THEN
            RAISE EXCEPTION 'Field key collision: A global field with key "%" already exists on business object %', 
                NEW.field_key, NEW.business_object_id
                USING ERRCODE = '23505';
        END IF;
    ELSE
        -- Case B: Global Field cannot collide with any tenant custom field key on the same business object
        IF EXISTS (
            SELECT 1 FROM configuration_business_object_fields
            WHERE business_object_id = NEW.business_object_id
              AND tenant_id IS NOT NULL
              AND field_key = NEW.field_key
              AND id != COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid)
        ) THEN
            RAISE EXCEPTION 'Field key collision: One or more tenant custom fields with key "%" already exist on business object %', 
                NEW.field_key, NEW.business_object_id
                USING ERRCODE = '23505';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_field_key_collision
    BEFORE INSERT OR UPDATE ON configuration_business_object_fields
    FOR EACH ROW EXECUTE FUNCTION fn_check_field_key_collision();


-- ============================================================================
-- STEP 3: SECURITY ENTITIES & MANIFEST RECORDING
-- ============================================================================

DO $$
DECLARE
    v_tenant_id  UUID := 'cf1757d5-c535-4335-939c-24908aee0209';
    v_role_id    UUID := '89b34568-f484-48ed-958e-177a3ca688ae';
    v_mod_id     UUID := 'c0000014-0000-0000-0000-000000000001';
    v_ent_id     UUID := 'c0000014-0000-0000-0000-000000000002';
    
    -- Permission IDs and Keys
    v_p_rec_read UUID := 'c0000014-0000-0000-0000-000000000003';
    v_p_rec_writ UUID := 'c0000014-0000-0000-0000-000000000004';
    v_p_fld_read UUID := 'c0000014-0000-0000-0000-000000000005';
    v_p_fld_mgt  UUID := 'c0000014-0000-0000-0000-000000000006';

    -- Role Permission Assignment IDs
    v_rp_rec_r   UUID := 'c0000014-0000-0000-0000-000000000007';
    v_rp_rec_w   UUID := 'c0000014-0000-0000-0000-000000000008';
    v_rp_fld_r   UUID := 'c0000014-0000-0000-0000-000000000009';
    v_rp_fld_m   UUID := 'c0000014-0000-0000-0000-000000000010';

    v_rec        RECORD;
BEGIN
    -- 3.1 Provision Module CONFIGURATION (Case 1/2/3)
    SELECT * INTO v_rec FROM configuration_modules WHERE module_key = 'CONFIGURATION';
    IF NOT FOUND THEN
        INSERT INTO configuration_modules (id, module_key, name, description, version_number, status, created_at, updated_at)
        VALUES (v_mod_id, 'CONFIGURATION', 'Configuration', 'MAX ERP configuration engine', 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        
        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'configuration_modules', v_mod_id);
    ELSE
        IF v_rec.id != v_mod_id OR v_rec.status != 'ACTIVE' THEN
            RAISE EXCEPTION 'Conflict on configuration_modules: CONFIGURATION exists with mismatched attributes';
        END IF;
        RAISE NOTICE 'Preserving matching pre-existing module CONFIGURATION (not claimed by V14)';
    END IF;

    -- 3.2 Provision Entitlement (Case 1/2/3)
    SELECT * INTO v_rec FROM configuration_module_entitlements WHERE tenant_id = v_tenant_id AND module_id = v_mod_id;
    IF NOT FOUND THEN
        INSERT INTO configuration_module_entitlements (id, tenant_id, module_id, status, enabled_at, created_at, updated_at)
        VALUES (v_ent_id, v_tenant_id, v_mod_id, 'ENABLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'configuration_module_entitlements', v_ent_id);
    ELSE
        IF v_rec.id != v_ent_id OR v_rec.status != 'ENABLED' THEN
            RAISE EXCEPTION 'Conflict on configuration_module_entitlements: Entitlement exists with mismatched attributes';
        END IF;
        RAISE NOTICE 'Preserving matching pre-existing entitlement (not claimed by V14)';
    END IF;

    -- 3.3 Provision 4 Permissions
    -- Permission 1: RECORD_READ
    SELECT * INTO v_rec FROM security_permissions WHERE permission_key = 'CONFIGURATION.RECORD_READ';
    IF NOT FOUND THEN
        INSERT INTO security_permissions (id, permission_key, name, description, module_key, action_key, created_at, updated_at)
        VALUES (v_p_rec_read, 'CONFIGURATION.RECORD_READ', 'Read Records', 'Allows reading records', 'CONFIGURATION', 'RECORD_READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        
        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_permissions', v_p_rec_read);
    ELSE
        IF v_rec.id != v_p_rec_read OR v_rec.module_key != 'CONFIGURATION' OR v_rec.action_key != 'RECORD_READ' THEN
            RAISE EXCEPTION 'Conflict on security_permissions: CONFIGURATION.RECORD_READ exists with mismatched attributes';
        END IF;
    END IF;

    -- Permission 2: RECORD_WRITE
    SELECT * INTO v_rec FROM security_permissions WHERE permission_key = 'CONFIGURATION.RECORD_WRITE';
    IF NOT FOUND THEN
        INSERT INTO security_permissions (id, permission_key, name, description, module_key, action_key, created_at, updated_at)
        VALUES (v_p_rec_writ, 'CONFIGURATION.RECORD_WRITE', 'Write Records', 'Allows creating and editing records', 'CONFIGURATION', 'RECORD_WRITE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        
        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_permissions', v_p_rec_writ);
    ELSE
        IF v_rec.id != v_p_rec_writ OR v_rec.module_key != 'CONFIGURATION' OR v_rec.action_key != 'RECORD_WRITE' THEN
            RAISE EXCEPTION 'Conflict on security_permissions: CONFIGURATION.RECORD_WRITE exists with mismatched attributes';
        END IF;
    END IF;

    -- Permission 3: FIELD_READ
    SELECT * INTO v_rec FROM security_permissions WHERE permission_key = 'CONFIGURATION.FIELD_READ';
    IF NOT FOUND THEN
        INSERT INTO security_permissions (id, permission_key, name, description, module_key, action_key, created_at, updated_at)
        VALUES (v_p_fld_read, 'CONFIGURATION.FIELD_READ', 'Read Fields', 'Allows reading field configurations', 'CONFIGURATION', 'FIELD_READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        
        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_permissions', v_p_fld_read);
    ELSE
        IF v_rec.id != v_p_fld_read OR v_rec.module_key != 'CONFIGURATION' OR v_rec.action_key != 'FIELD_READ' THEN
            RAISE EXCEPTION 'Conflict on security_permissions: CONFIGURATION.FIELD_READ exists with mismatched attributes';
        END IF;
    END IF;

    -- Permission 4: FIELD_MANAGE
    SELECT * INTO v_rec FROM security_permissions WHERE permission_key = 'CONFIGURATION.FIELD_MANAGE';
    IF NOT FOUND THEN
        INSERT INTO security_permissions (id, permission_key, name, description, module_key, action_key, created_at, updated_at)
        VALUES (v_p_fld_mgt, 'CONFIGURATION.FIELD_MANAGE', 'Manage Fields', 'Allows managing custom fields', 'CONFIGURATION', 'FIELD_MANAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        
        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_permissions', v_p_fld_mgt);
    ELSE
        IF v_rec.id != v_p_fld_mgt OR v_rec.module_key != 'CONFIGURATION' OR v_rec.action_key != 'FIELD_MANAGE' THEN
            RAISE EXCEPTION 'Conflict on security_permissions: CONFIGURATION.FIELD_MANAGE exists with mismatched attributes';
        END IF;
    END IF;

    -- 3.4 Provision Role Permissions for Security Test Manager
    -- Assignment 1: RECORD_READ
    SELECT * INTO v_rec FROM security_role_permissions WHERE role_id = v_role_id AND permission_id = v_p_rec_read;
    IF NOT FOUND THEN
        INSERT INTO security_role_permissions (id, role_id, permission_id, created_at, updated_at)
        VALUES (v_rp_rec_r, v_role_id, v_p_rec_read, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_role_permissions', v_rp_rec_r);
    ELSE
        IF v_rec.id != v_rp_rec_r THEN
            RAISE EXCEPTION 'Conflict on security_role_permissions: Assignment exists under different surrogate ID';
        END IF;
    END IF;

    -- Assignment 2: RECORD_WRITE
    SELECT * INTO v_rec FROM security_role_permissions WHERE role_id = v_role_id AND permission_id = v_p_rec_writ;
    IF NOT FOUND THEN
        INSERT INTO security_role_permissions (id, role_id, permission_id, created_at, updated_at)
        VALUES (v_rp_rec_w, v_role_id, v_p_rec_writ, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_role_permissions', v_rp_rec_w);
    ELSE
        IF v_rec.id != v_rp_rec_w THEN
            RAISE EXCEPTION 'Conflict on security_role_permissions: Assignment exists under different surrogate ID';
        END IF;
    END IF;

    -- Assignment 3: FIELD_READ
    SELECT * INTO v_rec FROM security_role_permissions WHERE role_id = v_role_id AND permission_id = v_p_fld_read;
    IF NOT FOUND THEN
        INSERT INTO security_role_permissions (id, role_id, permission_id, created_at, updated_at)
        VALUES (v_rp_fld_r, v_role_id, v_p_fld_read, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_role_permissions', v_rp_fld_r);
    ELSE
        IF v_rec.id != v_rp_fld_r THEN
            RAISE EXCEPTION 'Conflict on security_role_permissions: Assignment exists under different surrogate ID';
        END IF;
    END IF;

    -- Assignment 4: FIELD_MANAGE
    SELECT * INTO v_rec FROM security_role_permissions WHERE role_id = v_role_id AND permission_id = v_p_fld_mgt;
    IF NOT FOUND THEN
        INSERT INTO security_role_permissions (id, role_id, permission_id, created_at, updated_at)
        VALUES (v_rp_fld_m, v_role_id, v_p_fld_mgt, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

        INSERT INTO system_migration_manifest (migration_version, entity_table, entity_id)
        VALUES ('V14', 'security_role_permissions', v_rp_fld_m);
    ELSE
        IF v_rec.id != v_rp_fld_m THEN
            RAISE EXCEPTION 'Conflict on security_role_permissions: Assignment exists under different surrogate ID';
        END IF;
    END IF;

    RAISE NOTICE 'SECURITY ENTITIES PROVISIONED AND RECORDED CLEANLY';
END $$;


-- ============================================================================
-- STEP 4: DEEP CATALOG POSTFLIGHT ASSERTIONS
-- ============================================================================

DO $$
DECLARE
    v_cnt          INTEGER;
    v_ref_match    INTEGER;
    v_fn_def       TEXT;
BEGIN
    -- 4.1 Assert Tenant Backfill Integrity (Zero NULLs, exactly 10 records and 5 jobs)
    SELECT count(*) INTO v_cnt 
    FROM configuration_business_object_records 
    WHERE tenant_id = 'cf1757d5-c535-4335-939c-24908aee0209';
    IF v_cnt != 10 THEN RAISE EXCEPTION 'Postflight Failure: Expected 10 backfilled records, found %', v_cnt; END IF;

    SELECT count(*) INTO v_cnt 
    FROM configuration_business_object_records 
    WHERE tenant_id IS NULL;
    IF v_cnt != 0 THEN RAISE EXCEPTION 'Postflight Failure: Found % records with NULL tenant_id', v_cnt; END IF;

    SELECT count(*) INTO v_cnt 
    FROM configuration_import_jobs 
    WHERE tenant_id = 'cf1757d5-c535-4335-939c-24908aee0209';
    IF v_cnt != 5 THEN RAISE EXCEPTION 'Postflight Failure: Expected 5 backfilled jobs, found %', v_cnt; END IF;

    SELECT count(*) INTO v_cnt 
    FROM configuration_import_jobs 
    WHERE tenant_id IS NULL;
    IF v_cnt != 0 THEN RAISE EXCEPTION 'Postflight Failure: Found % import jobs with NULL tenant_id', v_cnt; END IF;

    -- 4.2 Assert Import Rows Remain Intact Under Parent Jobs (11 rows, 0 orphaned)
    SELECT count(*) INTO v_cnt FROM configuration_import_rows;
    IF v_cnt != 11 THEN RAISE EXCEPTION 'Postflight Failure: Expected 11 import rows, found %', v_cnt; END IF;

    IF EXISTS (
        SELECT 1 FROM configuration_import_rows r
        LEFT JOIN configuration_import_jobs j ON r.import_job_id = j.id
        WHERE j.id IS NULL
    ) THEN
        RAISE EXCEPTION 'Postflight Failure: Orphaned import rows detected';
    END IF;

    -- 4.3 Assert Global Fields Remain Global (16 fields with tenant_id IS NULL)
    SELECT count(*) INTO v_cnt 
    FROM configuration_business_object_fields 
    WHERE tenant_id IS NULL;
    IF v_cnt != 16 THEN RAISE EXCEPTION 'Postflight Failure: Expected 16 global fields with NULL tenant_id, found %', v_cnt; END IF;

    -- 4.4 Deep Assert Foreign Key Constraints with ON DELETE RESTRICT
    IF (
        SELECT count(*) FROM pg_constraint 
        WHERE conname IN ('fk_records_tenant', 'fk_import_jobs_tenant', 'fk_fields_tenant')
          AND contype = 'f'
          AND confdeltype = 'r' -- 'r' = RESTRICT
          AND confrelid = 'configuration_tenants'::regclass
    ) != 3 THEN
        RAISE EXCEPTION 'Postflight Failure: Foreign keys missing or not referencing configuration_tenants with RESTRICT';
    END IF;

    -- 4.5 Deep Assert Partial Unique Indexes & Predicates
    IF NOT EXISTS (
        SELECT 1 FROM pg_index idx
        JOIN pg_class c ON idx.indexrelid = c.oid
        WHERE c.relname = 'uq_fields_global_key'
          AND idx.indrelid = 'configuration_business_object_fields'::regclass
          AND idx.indisunique = TRUE
          AND pg_get_expr(idx.indpred, idx.indrelid) = '(tenant_id IS NULL)'
    ) THEN
        RAISE EXCEPTION 'Postflight Failure: Index uq_fields_global_key invalid or missing predicate';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_index idx
        JOIN pg_class c ON idx.indexrelid = c.oid
        WHERE c.relname = 'uq_fields_tenant_key'
          AND idx.indrelid = 'configuration_business_object_fields'::regclass
          AND idx.indisunique = TRUE
          AND pg_get_expr(idx.indpred, idx.indrelid) = '(tenant_id IS NOT NULL)'
    ) THEN
        RAISE EXCEPTION 'Postflight Failure: Index uq_fields_tenant_key invalid or missing predicate';
    END IF;

    -- 4.6 Deep Assert Collision Trigger Definition & Enabled State
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger 
        WHERE tgname = 'trg_check_field_key_collision' 
          AND tgrelid = 'configuration_business_object_fields'::regclass
          AND tgenabled = 'O'
          AND tgfoid = 'fn_check_field_key_collision'::regproc
    ) THEN
        RAISE EXCEPTION 'Postflight Failure: Trigger trg_check_field_key_collision not active or improperly mapped';
    END IF;

    -- 4.7 Deep Assert Trigger Function Body Contains Row Locks and Canonical Ordering
    SELECT pg_get_functiondef(oid) INTO v_fn_def 
    FROM pg_proc 
    WHERE proname = 'fn_check_field_key_collision' 
      AND pronamespace = 'public'::regnamespace;

    IF v_fn_def NOT LIKE '%FOR NO KEY UPDATE%' OR v_fn_def NOT LIKE '%LEAST(OLD.business_object_id, NEW.business_object_id)%' THEN
        RAISE EXCEPTION 'Postflight Failure: Trigger function missing parent row lock or canonical ordering logic';
    END IF;

    -- 4.8 Bit-for-Bit Assert Untouched Orphaned Reference Row
    SELECT count(*) INTO v_ref_match
    FROM configuration_business_object_field_references cur
    JOIN _v14_ref_baseline base ON cur.id = base.id
    WHERE cur.field_id = base.field_id
      AND cur.target_business_object_id = base.target_business_object_id
      AND cur.display_field_id = base.display_field_id
      AND cur.allow_multiple = base.allow_multiple
      AND cur.allow_create = base.allow_create
      AND cur.created_at = base.created_at
      AND cur.updated_at = base.updated_at;
    IF v_ref_match != 1 THEN
        RAISE EXCEPTION 'Postflight Failure: Reference row modified during migration execution';
    END IF;

    -- 4.9 Assert Absence of Unexpected CONFIGURATION Permissions
    IF (SELECT count(*) FROM security_permissions WHERE module_key = 'CONFIGURATION') != 4 THEN
        RAISE EXCEPTION 'Postflight Failure: Unexpected CONFIGURATION permissions found';
    END IF;

    -- 4.10 Assert Absence of Unexpected Role Assignments
    IF (
        SELECT count(*) FROM security_role_permissions 
        WHERE permission_id IN (SELECT id FROM security_permissions WHERE module_key = 'CONFIGURATION')
          AND role_id != '89b34568-f484-48ed-958e-177a3ca688ae'
    ) != 0 THEN
        RAISE EXCEPTION 'Postflight Failure: CONFIGURATION permissions assigned to unauthorized roles';
    END IF;

    RAISE NOTICE 'ALL DEEP CATALOG POSTFLIGHT ASSERTIONS VERIFIED CLEANLY';
END $$;

COMMIT;
