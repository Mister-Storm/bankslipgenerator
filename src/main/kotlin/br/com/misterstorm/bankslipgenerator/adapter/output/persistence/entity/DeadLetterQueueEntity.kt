package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity

import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity for Dead Letter Queue persistence (JDBC - no JPA annotations)
 * Aligned with V8 migration schema
 */
data class DeadLetterQueueEntity(
    val id: UUID = UUID.randomUUID(),
    val entityType: String, // e.g., "webhook", "bankslip", "cnab"
    val entityId: UUID,
    val payload: String, // JSON representation
    val errorMessage: String,
    val attempts: Int = 0,
    val lastAttemptAt: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val resolvedAt: LocalDateTime? = null,
    val resolvedBy: String? = null
)


