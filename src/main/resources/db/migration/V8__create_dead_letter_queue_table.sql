-- Create dead_letter_queue table for failed operations
CREATE TABLE dead_letter_queue (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    payload TEXT NOT NULL,
    error_message TEXT NOT NULL,
    attempts INT NOT NULL,
    last_attempt_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255)
);

CREATE INDEX idx_dlq_entity_type ON dead_letter_queue(entity_type);
CREATE INDEX idx_dlq_created ON dead_letter_queue(created_at);
CREATE INDEX idx_dlq_resolved ON dead_letter_queue(resolved_at);
CREATE INDEX idx_dlq_pending ON dead_letter_queue(entity_type, resolved_at) WHERE resolved_at IS NULL;

-- Add comment
COMMENT ON TABLE dead_letter_queue IS 'Stores failed operations for manual resolution';

