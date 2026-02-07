package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.IdempotencyKeyEntity
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class JdbcIdempotencyRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val rowMapper: IdempotencyKeyRowMapper
) {
    fun save(entity: IdempotencyKeyEntity): IdempotencyKeyEntity {
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            IdempotencySql.INSERT,
            entity.id,
            entity.key,
            entity.endpoint,
            entity.requestHash,
            entity.responseBody,
            entity.statusCode,
            now,
            entity.expiresAt
        )

        return entity.copy(createdAt = now)
    }

    fun findByKey(key: String): IdempotencyKeyEntity? {
        return try {
            jdbcTemplate.queryForObject(IdempotencySql.FIND_BY_KEY, rowMapper, key)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun deleteExpired(): Int {
        return jdbcTemplate.update(IdempotencySql.DELETE_EXPIRED)
    }

    fun countActive(): Long {
        return jdbcTemplate.queryForObject(IdempotencySql.COUNT_ACTIVE, Long::class.java) ?: 0L
    }
}

