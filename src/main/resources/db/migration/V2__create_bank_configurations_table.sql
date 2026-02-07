-- Create bank_configurations table
CREATE TABLE bank_configurations (
    id UUID PRIMARY KEY,
    bank_code VARCHAR(10) NOT NULL UNIQUE,
    bank_name VARCHAR(255) NOT NULL,
    layout_configuration JSONB NOT NULL,
    cnab_configuration JSONB NOT NULL,
    validation_rules JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bank_configurations_bank_code ON bank_configurations(bank_code);
CREATE INDEX idx_bank_configurations_is_active ON bank_configurations(is_active);

