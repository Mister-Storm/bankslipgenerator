-- Create idempotency_keys table for idempotent requests
CREATE TABLE idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    endpoint VARCHAR(500) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT NOT NULL,
    status_code INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
CREATE INDEX idx_idempotency_created ON idempotency_keys(created_at);

-- Add comment
COMMENT ON TABLE idempotency_keys IS 'Stores idempotency keys for duplicate request prevention';

