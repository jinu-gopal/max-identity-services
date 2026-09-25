CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(320) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);