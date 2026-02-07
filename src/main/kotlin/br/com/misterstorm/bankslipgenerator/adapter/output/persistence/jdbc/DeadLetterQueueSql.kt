package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

object DeadLetterQueueSql {
    const val INSERT = """
        INSERT INTO dead_letter_queue (
            id, entity_type, entity_id, payload, error_message,
            attempts, last_attempt_at, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """

    const val UPDATE_ATTEMPTS = """
        UPDATE dead_letter_queue SET
            attempts = ?,
            error_message = ?,
            last_attempt_at = ?
        WHERE id = ?
    """

    const val MARK_RESOLVED = """
        UPDATE dead_letter_queue SET
            resolved_at = ?,
            resolved_by = ?
        WHERE id = ?
    """

    const val FIND_BY_ID = """
        SELECT id, entity_type, entity_id, payload, error_message,
               attempts, last_attempt_at, created_at, resolved_at, resolved_by
        FROM dead_letter_queue
        WHERE id = ?
    """

    const val FIND_PENDING = """
        SELECT id, entity_type, entity_id, payload, error_message,
               attempts, last_attempt_at, created_at, resolved_at, resolved_by
        FROM dead_letter_queue
        WHERE resolved_at IS NULL
        ORDER BY created_at DESC
    """

    const val FIND_BY_ENTITY_TYPE = """
        SELECT id, entity_type, entity_id, payload, error_message,
               attempts, last_attempt_at, created_at, resolved_at, resolved_by
        FROM dead_letter_queue
        WHERE entity_type = ? AND resolved_at IS NULL
        ORDER BY created_at DESC
    """

    const val FIND_BY_ENTITY = """
        SELECT id, entity_type, entity_id, payload, error_message,
               attempts, last_attempt_at, created_at, resolved_at, resolved_by
        FROM dead_letter_queue
        WHERE entity_type = ? AND entity_id = ?
        ORDER BY created_at DESC
    """

    const val COUNT_PENDING = """
        SELECT COUNT(*) 
        FROM dead_letter_queue 
        WHERE resolved_at IS NULL
    """

    const val DELETE_RESOLVED_OLDER_THAN = """
        DELETE FROM dead_letter_queue
        WHERE resolved_at IS NOT NULL AND resolved_at < ?
    """
}

