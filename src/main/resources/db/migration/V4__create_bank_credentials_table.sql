-- Create bank_credentials table for encrypted credentials
CREATE TABLE bank_credentials (
    id UUID PRIMARY KEY,
    bank_code VARCHAR(10) NOT NULL UNIQUE,
    credential_type VARCHAR(20) NOT NULL,
    encrypted_client_id TEXT,
    encrypted_client_secret TEXT,
    encrypted_api_key TEXT,
    encrypted_certificate TEXT,
    encrypted_certificate_password TEXT,
    encrypted_username TEXT,
    encrypted_password TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_accessed_at TIMESTAMP
);

CREATE INDEX idx_bank_credentials_bank_code ON bank_credentials(bank_code);
CREATE INDEX idx_bank_credentials_credential_type ON bank_credentials(credential_type);

-- Add audit log for credential access
CREATE TABLE credential_access_log (
    id UUID PRIMARY KEY,
    credential_id UUID NOT NULL REFERENCES bank_credentials(id),
    accessed_by VARCHAR(255) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operation VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45)
);

CREATE INDEX idx_credential_access_log_credential_id ON credential_access_log(credential_id);
CREATE INDEX idx_credential_access_log_accessed_at ON credential_access_log(accessed_at);

