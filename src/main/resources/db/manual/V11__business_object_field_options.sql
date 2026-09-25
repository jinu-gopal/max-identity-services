-- ============================================================
-- MAX ERP Identity Service
-- Manual Database Version V11
-- Business Object Field Options
-- ============================================================
--
-- Purpose:
--   Supports configurable options for CHOICE and MULTI_CHOICE
--   business object fields.
--
-- Database convention:
--   UUIDs and audit timestamps are supplied by the application.
--   No UUID-generation extension or database default is required.
--
-- ============================================================

CREATE TABLE IF NOT EXISTS configuration_business_object_field_options (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL,
    option_value VARCHAR(200) NOT NULL,
    option_label VARCHAR(200) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_business_object_field_options_field
        FOREIGN KEY (field_id)
        REFERENCES configuration_business_object_fields(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_business_object_field_options_display_order
        CHECK (display_order >= 0),

    CONSTRAINT uq_business_object_field_options_value
        UNIQUE (field_id, option_value)
);

CREATE INDEX IF NOT EXISTS idx_business_object_field_options_field
    ON configuration_business_object_field_options(field_id);

CREATE INDEX IF NOT EXISTS idx_business_object_field_options_order
    ON configuration_business_object_field_options(field_id, display_order);

CREATE INDEX IF NOT EXISTS idx_business_object_field_options_active
    ON configuration_business_object_field_options(field_id, active);

-- ============================================================
-- End V11
-- ============================================================