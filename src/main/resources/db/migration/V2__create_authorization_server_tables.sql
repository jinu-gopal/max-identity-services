CREATE TABLE oauth2_registered_client (
                                          id VARCHAR(100) PRIMARY KEY,
                                          client_id VARCHAR(100) NOT NULL UNIQUE,
                                          client_id_issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          client_secret VARCHAR(200) NULL,
                                          client_secret_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                          client_name VARCHAR(200) NOT NULL,
                                          client_authentication_methods VARCHAR(1000) NOT NULL,
                                          authorization_grant_types VARCHAR(1000) NOT NULL,
                                          redirect_uris VARCHAR(1000) NULL,
                                          post_logout_redirect_uris VARCHAR(1000) NULL,
                                          scopes VARCHAR(1000) NOT NULL,
                                          client_settings VARCHAR(2000) NOT NULL,
                                          token_settings VARCHAR(2000) NOT NULL
);

CREATE TABLE oauth2_authorization (
                                      id VARCHAR(100) PRIMARY KEY,
                                      registered_client_id VARCHAR(100) NOT NULL,
                                      principal_name VARCHAR(200) NOT NULL,
                                      authorization_grant_type VARCHAR(100) NOT NULL,
                                      authorized_scopes VARCHAR(1000) NULL,
                                      attributes TEXT NULL,
                                      state VARCHAR(500) NULL,
                                      authorization_code_value TEXT NULL,
                                      authorization_code_issued_at TIMESTAMP WITH TIME ZONE NULL,
                                      authorization_code_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                      authorization_code_metadata TEXT NULL,
                                      access_token_value TEXT NULL,
                                      access_token_issued_at TIMESTAMP WITH TIME ZONE NULL,
                                      access_token_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                      access_token_metadata TEXT NULL,
                                      access_token_type VARCHAR(100) NULL,
                                      access_token_scopes VARCHAR(1000) NULL,
                                      oidc_id_token_value TEXT NULL,
                                      oidc_id_token_issued_at TIMESTAMP WITH TIME ZONE NULL,
                                      oidc_id_token_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                      oidc_id_token_metadata TEXT NULL,
                                      refresh_token_value TEXT NULL,
                                      refresh_token_issued_at TIMESTAMP WITH TIME ZONE NULL,
                                      refresh_token_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                      refresh_token_metadata TEXT NULL,
                                      user_code_value TEXT NULL,
                                      user_code_issued_at TIMESTAMP WITH TIME ZONE NULL,
                                      user_code_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                      user_code_metadata TEXT NULL,
                                      device_code_value TEXT NULL,
                                      device_code_issued_at TIMESTAMP WITH TIME ZONE NULL,
                                      device_code_expires_at TIMESTAMP WITH TIME ZONE NULL,
                                      device_code_metadata TEXT NULL
);

CREATE TABLE oauth2_authorization_consent (
                                              registered_client_id VARCHAR(100) NOT NULL,
                                              principal_name VARCHAR(200) NOT NULL,
                                              authorities VARCHAR(1000) NOT NULL,
                                              PRIMARY KEY (registered_client_id, principal_name)
);

CREATE INDEX idx_oauth2_authorization_registered_client
    ON oauth2_authorization (registered_client_id);

CREATE INDEX idx_oauth2_authorization_principal_name
    ON oauth2_authorization (principal_name);