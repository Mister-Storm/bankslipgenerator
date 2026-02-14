package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity

import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity for idempotency key persistence (JDBC - no JPA annotations)
 */
data class IdempotencyKeyEntity(
    val id: UUID = UUID.randomUUID(),
    val key: String,
    val endpoint: String,
    val requestHash: String,
    val responseBody: String,
    val statusCode: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime
)
