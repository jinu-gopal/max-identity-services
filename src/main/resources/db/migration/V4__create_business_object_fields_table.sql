CREATE TABLE configuration_business_object_fields (
                                                      id UUID PRIMARY KEY,

                                                      business_object_id UUID NOT NULL,

                                                      field_key VARCHAR(100) NOT NULL,
                                                      label VARCHAR(150) NOT NULL,
                                                      description VARCHAR(1000),

                                                      field_type VARCHAR(50) NOT NULL,

                                                      required BOOLEAN NOT NULL DEFAULT FALSE,
                                                      unique_value BOOLEAN NOT NULL DEFAULT FALSE,
                                                      read_only BOOLEAN NOT NULL DEFAULT FALSE,
                                                      hidden BOOLEAN NOT NULL DEFAULT FALSE,

                                                      default_value VARCHAR(2000),

                                                      min_value NUMERIC(38, 10),
                                                      max_value NUMERIC(38, 10),

                                                      validation_pattern VARCHAR(1000),

                                                      display_order INTEGER NOT NULL DEFAULT 0,

                                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                                      CONSTRAINT fk_business_object_fields_business_object
                                                          FOREIGN KEY (business_object_id)
                                                              REFERENCES configuration_business_objects(id)
                                                              ON DELETE CASCADE,

                                                      CONSTRAINT uq_business_object_fields_key
                                                          UNIQUE (business_object_id, field_key),

                                                      CONSTRAINT chk_business_object_fields_type
                                                          CHECK (
                                                              field_type IN (
                                                                             'TEXT',
                                                                             'LONG_TEXT',
                                                                             'NUMBER',
                                                                             'DECIMAL',
                                                                             'CURRENCY',
                                                                             'PERCENTAGE',
                                                                             'DATE',
                                                                             'DATETIME',
                                                                             'BOOLEAN',
                                                                             'CHOICE',
                                                                             'MULTI_CHOICE',
                                                                             'REFERENCE',
                                                                             'USER',
                                                                             'EMAIL',
                                                                             'PHONE',
                                                                             'ADDRESS',
                                                                             'FILE',
                                                                             'IMAGE',
                                                                             'FORMULA',
                                                                             'AUTO_NUMBER'
                                                                  )
                                                              ),

                                                      CONSTRAINT chk_business_object_fields_display_order
                                                          CHECK (display_order >= 0)
);

CREATE INDEX idx_business_object_fields_business_object
    ON configuration_business_object_fields (business_object_id);

CREATE INDEX idx_business_object_fields_type
    ON configuration_business_object_fields (field_type);

CREATE INDEX idx_business_object_fields_order
    ON configuration_business_object_fields (
                                             business_object_id,
                                             display_order
        );