-- Create cnab_files table
CREATE TABLE cnab_files (
    id UUID PRIMARY KEY,
    bank_code VARCHAR(10) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    version VARCHAR(20) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_content TEXT NOT NULL,
    file_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    total_records INT NOT NULL,
    processed_records INT NOT NULL DEFAULT 0,
    error_records INT NOT NULL DEFAULT 0,
    errors JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_cnab_files_bank_code ON cnab_files(bank_code);
CREATE INDEX idx_cnab_files_file_type ON cnab_files(file_type);
CREATE INDEX idx_cnab_files_status ON cnab_files(status);
CREATE INDEX idx_cnab_files_created_at ON cnab_files(created_at);

