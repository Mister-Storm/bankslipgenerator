package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.DeadLetterQueueEntity
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

class DeadLetterQueueRowMapper : org.springframework.jdbc.core.RowMapper<DeadLetterQueueEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): DeadLetterQueueEntity {
        return DeadLetterQueueEntity(
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
}

