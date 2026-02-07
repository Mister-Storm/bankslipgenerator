-- Create webhook_deliveries table
CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY,
    webhook_config_id UUID NOT NULL REFERENCES webhook_configs(id) ON DELETE CASCADE,
    bankslip_id UUID NOT NULL REFERENCES bankslips(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    url VARCHAR(500) NOT NULL,
    status_code INT,
    response_body TEXT,
    attempt_number INT NOT NULL DEFAULT 1,
    delivered_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_deliveries_webhook_config_id ON webhook_deliveries(webhook_config_id);
CREATE INDEX idx_webhook_deliveries_bankslip_id ON webhook_deliveries(bankslip_id);
CREATE INDEX idx_webhook_deliveries_event_type ON webhook_deliveries(event_type);
CREATE INDEX idx_webhook_deliveries_created_at ON webhook_deliveries(created_at);
CREATE INDEX idx_webhook_deliveries_delivered_at ON webhook_deliveries(delivered_at);

-- Add index for failed deliveries that need retry
CREATE INDEX idx_webhook_deliveries_failed ON webhook_deliveries(webhook_config_id, attempt_number)
    WHERE delivered_at IS NULL AND error_message IS NOT NULL;

