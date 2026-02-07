package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

/**
 * SQL queries for Webhook operations
 */
object WebhookSql {

    // Webhook Config
    const val INSERT_CONFIG = """
        INSERT INTO webhook_configs (
            id, client_id, url, secret, events, is_active,
            max_retries, retry_delay, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
    """

    const val UPDATE_CONFIG = """
        UPDATE webhook_configs SET
            client_id = ?, url = ?, secret = ?, events = ?::jsonb,
            is_active = ?, max_retries = ?, retry_delay = ?, updated_at = ?
        WHERE id = ?
    """

    const val FIND_CONFIG_BY_ID = """
        SELECT * FROM webhook_configs WHERE id = ?
    """

    const val FIND_CONFIG_BY_CLIENT_ID = """
        SELECT * FROM webhook_configs WHERE client_id = ?
    """

    const val FIND_ALL_ACTIVE_CONFIGS = """
        SELECT * FROM webhook_configs WHERE is_active = true
    """

    const val DELETE_CONFIG = """
        DELETE FROM webhook_configs WHERE id = ?
    """

    // Webhook Delivery
    const val INSERT_DELIVERY = """
        INSERT INTO webhook_deliveries (
            id, webhook_config_id, bankslip_id, event_type, payload,
            url, status_code, response_body, attempt_number,
            delivered_at, error_message, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    const val FIND_DELIVERIES_BY_CONFIG = """
        SELECT * FROM webhook_deliveries
        WHERE webhook_config_id = ?
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    """

    const val FIND_DELIVERIES_BY_BANKSLIP = """
        SELECT * FROM webhook_deliveries
        WHERE bankslip_id = ?
        ORDER BY created_at DESC
    """

    const val FIND_FAILED_DELIVERIES = """
        SELECT * FROM webhook_deliveries
        WHERE delivered_at IS NULL 
        AND error_message IS NOT NULL
        AND attempt_number < ?
        ORDER BY created_at ASC
        LIMIT 100
    """
}

