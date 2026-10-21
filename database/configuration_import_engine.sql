-- ============================================================
-- MAX ERP
-- Generic Configuration Import Engine
-- PostgreSQL 18
-- Manual SQL
-- ============================================================

CREATE TABLE configuration_import_jobs (
    id uuid NOT NULL,
    business_object_id uuid NOT NULL,

    file_name varchar(255) NOT NULL,
    file_type varchar(20) NOT NULL,
    file_size_bytes bigint,

    status varchar(40) NOT NULL DEFAULT 'UPLOADED',

    total_rows integer NOT NULL DEFAULT 0,
    valid_rows integer NOT NULL DEFAULT 0,
    invalid_rows integer NOT NULL DEFAULT 0,
    duplicate_rows integer NOT NULL DEFAULT 0,
    processed_rows integer NOT NULL DEFAULT 0,

    column_mapping jsonb,
    import_options jsonb,
    summary jsonb,

    created_by uuid,
    created_at timestamp with time zone NOT NULL,
    started_at timestamp with time zone,
    completed_at timestamp with time zone,
    updated_at timestamp with time zone NOT NULL,

    CONSTRAINT configuration_import_jobs_pkey
        PRIMARY KEY (id),

    CONSTRAINT fk_configuration_import_jobs_business_object
        FOREIGN KEY (business_object_id)
        REFERENCES configuration_business_objects(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_configuration_import_jobs_file_type
        CHECK (file_type IN ('CSV', 'XLSX')),

    CONSTRAINT chk_configuration_import_jobs_status
        CHECK (
            status IN (
                'UPLOADED',
                'MAPPING',
                'VALIDATING',
                'VALIDATED',
                'EXECUTING',
                'COMPLETED',
                'COMPLETED_WITH_ERRORS',
                'FAILED'
            )
        ),

    CONSTRAINT chk_configuration_import_jobs_total_rows
        CHECK (total_rows >= 0),

    CONSTRAINT chk_configuration_import_jobs_valid_rows
        CHECK (valid_rows >= 0),

    CONSTRAINT chk_configuration_import_jobs_invalid_rows
        CHECK (invalid_rows >= 0),

    CONSTRAINT chk_configuration_import_jobs_duplicate_rows
        CHECK (duplicate_rows >= 0),

    CONSTRAINT chk_configuration_import_jobs_processed_rows
        CHECK (processed_rows >= 0)
);

CREATE INDEX idx_configuration_import_jobs_business_object
    ON configuration_import_jobs (business_object_id);

CREATE INDEX idx_configuration_import_jobs_status
    ON configuration_import_jobs (status);

CREATE INDEX idx_configuration_import_jobs_created_at
    ON configuration_import_jobs (created_at);

CREATE INDEX idx_configuration_import_jobs_business_object_created_at
    ON configuration_import_jobs (business_object_id, created_at);


CREATE TABLE configuration_import_rows (
    id uuid NOT NULL,
    import_job_id uuid NOT NULL,

    row_number integer NOT NULL,

    status varchar(30) NOT NULL DEFAULT 'PENDING',

    raw_data jsonb NOT NULL DEFAULT '{}'::jsonb,
    normalized_data jsonb,

    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,

    CONSTRAINT configuration_import_rows_pkey
        PRIMARY KEY (id),

    CONSTRAINT fk_configuration_import_rows_import_job
        FOREIGN KEY (import_job_id)
        REFERENCES configuration_import_jobs(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_configuration_import_rows_job_row
        UNIQUE (import_job_id, row_number),

    CONSTRAINT chk_configuration_import_rows_status
        CHECK (
            status IN (
                'PENDING',
                'VALID',
                'INVALID',
                'DUPLICATE',
                'IMPORTED',
                'FAILED'
            )
        ),

    CONSTRAINT chk_configuration_import_rows_row_number
        CHECK (row_number > 0)
);

CREATE INDEX idx_configuration_import_rows_import_job
    ON configuration_import_rows (import_job_id);

CREATE INDEX idx_configuration_import_rows_status
    ON configuration_import_rows (import_job_id, status);

CREATE INDEX idx_configuration_import_rows_row_number
    ON configuration_import_rows (import_job_id, row_number);


CREATE TABLE configuration_import_errors (
    id uuid NOT NULL,

    import_job_id uuid NOT NULL,
    import_row_id uuid,

    row_number integer,
    column_name varchar(150),

    error_code varchar(80) NOT NULL,
    error_message varchar(1000) NOT NULL,

    source_value text,
    details jsonb,

    created_at timestamp with time zone NOT NULL,

    CONSTRAINT configuration_import_errors_pkey
        PRIMARY KEY (id),

    CONSTRAINT fk_configuration_import_errors_import_job
        FOREIGN KEY (import_job_id)
        REFERENCES configuration_import_jobs(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_configuration_import_errors_import_row
        FOREIGN KEY (import_row_id)
        REFERENCES configuration_import_rows(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_configuration_import_errors_row_number
        CHECK (
            row_number IS NULL OR row_number > 0
        )
);

CREATE INDEX idx_configuration_import_errors_import_job
    ON configuration_import_errors (import_job_id);

CREATE INDEX idx_configuration_import_errors_import_row
    ON configuration_import_errors (import_row_id);

CREATE INDEX idx_configuration_import_errors_row_number
    ON configuration_import_errors (import_job_id, row_number);

CREATE INDEX idx_configuration_import_errors_code
    ON configuration_import_errors (import_job_id, error_code);
