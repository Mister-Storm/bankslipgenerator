package br.com.misterstorm.bankslipgenerator.infrastructure.service

import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Dead Letter Queue Service
 * Manages failed operations that need manual intervention
 */
@Service
class DeadLetterQueueService(
    private val jdbcTemplate: JdbcTemplate,
    private val logger: Logger
) {

    data class DLQRecord(
        val id: UUID,
        val entityType: String,
        val entityId: UUID,
        val payload: String,
        val errorMessage: String,
        val attempts: Int,
        val lastAttemptAt: LocalDateTime,
        val createdAt: LocalDateTime,
        val resolvedAt: LocalDateTime? = null,
        val resolvedBy: String? = null
    )

    private val rowMapper = RowMapper<DLQRecord> { rs, _ ->
        DLQRecord(
            id = UUID.fromString(rs.getString("id")),
            entityType = rs.getString("entity_type"),
            entityId = UUID.fromString(rs.getString("entity_id")),
            payload = rs.getString("payload"),
            errorMessage = rs.getString("error_message"),
            attempts = rs.getInt("attempts"),
            lastAttemptAt = rs.getTimestamp("last_attempt_at").toLocalDateTime(),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            resolvedAt = rs.getTimestamp("resolved_at")?.toLocalDateTime(),
            resolvedBy = rs.getString("resolved_by")
        )
    }

    /**
     * Add failed operation to DLQ
     */
    fun addToQueue(
        entityType: String,
        entityId: UUID,
        payload: String,
        errorMessage: String,
        attempts: Int = 3
    ) {
        try {
            jdbcTemplate.update(
                """
                INSERT INTO dead_letter_queue 
                (id, entity_type, entity_id, payload, error_message, attempts, last_attempt_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """,
                UUID.randomUUID(),
                entityType,
                entityId,
                payload,
                errorMessage,
                attempts
            )

            logger.warn(
                "Added to Dead Letter Queue",
                "entityType" to entityType,
                "entityId" to entityId.toString(),
                "attempts" to attempts
            )
        } catch (e: Exception) {
            logger.error("Failed to add to DLQ", e)
        }
    }

    /**
     * Get all pending (unresolved) DLQ records
     */
    fun getPending(): List<DLQRecord> {
        return try {
            jdbcTemplate.query(
                """
                SELECT * FROM dead_letter_queue 
                WHERE resolved_at IS NULL 
                ORDER BY created_at ASC
                """,
                rowMapper
            )
        } catch (e: Exception) {
            logger.error("Failed to get pending DLQ records", e)
            emptyList()
        }
    }

    /**
     * Get DLQ records by entity type
     */
    fun getByEntityType(entityType: String): List<DLQRecord> {
        return try {
            jdbcTemplate.query(
                """
                SELECT * FROM dead_letter_queue 
                WHERE entity_type = ? AND resolved_at IS NULL
                ORDER BY created_at ASC
                """,
                rowMapper,
                entityType
            )
        } catch (e: Exception) {
            logger.error("Failed to get DLQ records by type", e)
            emptyList()
        }
    }

    /**
     * Mark DLQ record as resolved
     */
    fun markAsResolved(id: UUID, resolvedBy: String) {
        try {
            val rowsAffected = jdbcTemplate.update(
                """
                UPDATE dead_letter_queue 
                SET resolved_at = NOW(), resolved_by = ?
                WHERE id = ? AND resolved_at IS NULL
                """,
                resolvedBy,
                id
            )

            if (rowsAffected > 0) {
                logger.info(
                    "DLQ record marked as resolved",
                    "id" to id.toString(),
                    "resolvedBy" to resolvedBy
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to mark DLQ record as resolved", e)
        }
    }

    /**
     * Get statistics about DLQ
     */
    fun getStatistics(): Map<String, Any> {
        return try {
            val stats = jdbcTemplate.queryForMap(
                """
                SELECT 
                    COUNT(*) as total,
                    COUNT(CASE WHEN resolved_at IS NULL THEN 1 END) as pending,
                    COUNT(CASE WHEN resolved_at IS NOT NULL THEN 1 END) as resolved
                FROM dead_letter_queue
                """
            )
            
            val byType = jdbcTemplate.queryForList(
                """
                SELECT entity_type, COUNT(*) as count
                FROM dead_letter_queue
                WHERE resolved_at IS NULL
                GROUP BY entity_type
                """
            )

            mapOf(
                "total" to (stats["total"] ?: 0),
                "pending" to (stats["pending"] ?: 0),
                "resolved" to (stats["resolved"] ?: 0),
                "by_type" to byType
            )
        } catch (e: Exception) {
            logger.error("Failed to get DLQ statistics", e)
            emptyMap()
        }
    }

    /**
     * Clean up resolved records older than retention period
     */
    fun cleanupResolved(retentionDays: Int = 30) {
        try {
            val deletedCount = jdbcTemplate.update(
                """
                DELETE FROM dead_letter_queue
                WHERE resolved_at IS NOT NULL 
                AND resolved_at < NOW() - INTERVAL '$retentionDays days'
                """
            )

            logger.info(
                "Cleaned up resolved DLQ records",
                "deletedCount" to deletedCount,
                "retentionDays" to retentionDays
            )
        } catch (e: Exception) {
            logger.error("Failed to cleanup DLQ", e)
        }
    }
}

