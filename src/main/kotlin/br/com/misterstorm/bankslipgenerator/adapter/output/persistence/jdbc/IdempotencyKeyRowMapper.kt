package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.IdempotencyKeyEntity
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

class IdempotencyKeyRowMapper : org.springframework.jdbc.core.RowMapper<IdempotencyKeyEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): IdempotencyKeyEntity {
        return IdempotencyKeyEntity(
            id = UUID.fromString(rs.getString("id")),
            key = rs.getString("key"),
            endpoint = rs.getString("endpoint"),
            requestHash = rs.getString("request_hash"),
            responseBody = rs.getString("response_body"),
            statusCode = rs.getInt("status_code"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            expiresAt = rs.getTimestamp("expires_at").toLocalDateTime()
        )
    }
}

