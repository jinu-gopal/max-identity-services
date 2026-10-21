CREATE TABLE IF NOT EXISTS configuration_business_object_field_references (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL,
    target_business_object_id UUID NOT NULL,
    display_field_id UUID,
    allow_multiple BOOLEAN NOT NULL DEFAULT FALSE,
    allow_create BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_field_references_field
        FOREIGN KEY (field_id)
        REFERENCES configuration_business_object_fields(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_field_references_target_object
        FOREIGN KEY (target_business_object_id)
        REFERENCES configuration_business_objects(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_field_references_display_field
        FOREIGN KEY (display_field_id)
        REFERENCES configuration_business_object_fields(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_field_references_field
        UNIQUE (field_id)
);

CREATE INDEX IF NOT EXISTS idx_field_references_target_object
    ON configuration_business_object_field_references(target_business_object_id);

CREATE INDEX IF NOT EXISTS idx_field_references_display_field
    ON configuration_business_object_field_references(display_field_id);