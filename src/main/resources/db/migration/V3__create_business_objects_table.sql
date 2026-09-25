CREATE TABLE configuration_business_objects (
                                                id UUID PRIMARY KEY,
                                                object_key VARCHAR(100) NOT NULL UNIQUE,
                                                name VARCHAR(150) NOT NULL,
                                                description VARCHAR(1000),
                                                status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
                                                version_number INTEGER NOT NULL DEFAULT 1,
                                                created_by UUID NULL,
                                                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                                CONSTRAINT chk_configuration_business_objects_status
                                                    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),

                                                CONSTRAINT chk_configuration_business_objects_version
                                                    CHECK (version_number > 0)
);

CREATE INDEX idx_configuration_business_objects_status
    ON configuration_business_objects (status);

CREATE INDEX idx_configuration_business_objects_name
    ON configuration_business_objects (name);