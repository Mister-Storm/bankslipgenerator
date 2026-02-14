package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.DeadLetterQueueEntity
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class JdbcDeadLetterQueueRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val rowMapper: DeadLetterQueueRowMapper
) {
    fun save(entity: DeadLetterQueueEntity): DeadLetterQueueEntity {
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            DeadLetterQueueSql.INSERT,
            entity.id,
            entity.entityType,
            entity.entityId,
            entity.payload,
            entity.errorMessage,
            entity.attempts,
            entity.lastAttemptAt,
            now
        )

        return entity.copy(createdAt = now)
    }

    fun updateAttempts(id: UUID, attempts: Int, errorMessage: String) {
        jdbcTemplate.update(
            DeadLetterQueueSql.UPDATE_ATTEMPTS,
            attempts,
            errorMessage,
            LocalDateTime.now(),
            id
        )
    }

    fun markAsResolved(id: UUID, resolvedBy: String) {
        jdbcTemplate.update(
            DeadLetterQueueSql.MARK_RESOLVED,
            LocalDateTime.now(),
            resolvedBy,
            id
        )
    }

    fun findById(id: UUID): DeadLetterQueueEntity? {
        return try {
            jdbcTemplate.queryForObject(DeadLetterQueueSql.FIND_BY_ID, rowMapper, id)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findPending(): List<DeadLetterQueueEntity> {
        return jdbcTemplate.query(DeadLetterQueueSql.FIND_PENDING, rowMapper)
    }

    fun findByEntityType(entityType: String): List<DeadLetterQueueEntity> {
        return jdbcTemplate.query(DeadLetterQueueSql.FIND_BY_ENTITY_TYPE, rowMapper, entityType)
    }

    fun findByEntity(entityType: String, entityId: UUID): List<DeadLetterQueueEntity> {
        return jdbcTemplate.query(DeadLetterQueueSql.FIND_BY_ENTITY, rowMapper, entityType, entityId)
    }

    fun countPending(): Long {
        return jdbcTemplate.queryForObject(DeadLetterQueueSql.COUNT_PENDING, Long::class.java) ?: 0L
    }

    fun deleteResolvedOlderThan(date: LocalDateTime): Int {
        return jdbcTemplate.update(DeadLetterQueueSql.DELETE_RESOLVED_OLDER_THAN, date)
    }
}

