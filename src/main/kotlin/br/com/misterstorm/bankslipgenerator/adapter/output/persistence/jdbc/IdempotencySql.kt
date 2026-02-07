package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

object IdempotencySql {
    const val INSERT = """
        INSERT INTO idempotency_keys (
            id, key, endpoint, request_hash, response_body, 
            status_code, created_at, expires_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (key) DO NOTHING
    """

    const val FIND_BY_KEY = """
        SELECT id, key, endpoint, request_hash, response_body, 
               status_code, created_at, expires_at
        FROM idempotency_keys
        WHERE key = ? AND expires_at > NOW()
    """

    const val DELETE_EXPIRED = """
        DELETE FROM idempotency_keys
        WHERE expires_at < NOW()
    """

    const val COUNT_ACTIVE = """
        SELECT COUNT(*) 
        FROM idempotency_keys 
        WHERE expires_at > NOW()
    """
}

