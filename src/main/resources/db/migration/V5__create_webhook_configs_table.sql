-- Create webhook_configs table
CREATE TABLE webhook_configs (
    id UUID PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    events JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    max_retries INT NOT NULL DEFAULT 3,
    retry_delay INT NOT NULL DEFAULT 5000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_configs_client_id ON webhook_configs(client_id);
CREATE INDEX idx_webhook_configs_is_active ON webhook_configs(is_active);
CREATE INDEX idx_webhook_configs_url ON webhook_configs(url);

