CREATE TABLE IF NOT EXISTS configuration_business_object_records (
    id UUID PRIMARY KEY,
    business_object_id UUID NOT NULL,
    record_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_business_object_records_business_object
        FOREIGN KEY (business_object_id)
        REFERENCES configuration_business_objects(id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_business_object_records_business_object
    ON configuration_business_object_records(business_object_id);

CREATE INDEX IF NOT EXISTS idx_business_object_records_created_at
    ON configuration_business_object_records(created_at);

CREATE INDEX IF NOT EXISTS idx_business_object_records_data
    ON configuration_business_object_records
    USING GIN(record_data);