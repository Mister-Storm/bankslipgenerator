package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.WebhookConfigEntity
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.WebhookDeliveryEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

@Repository
class JdbcWebhookRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val configMapper = WebhookConfigEntityRowMapper()
    private val deliveryMapper = WebhookDeliveryEntityRowMapper()

    fun saveConfig(entity: WebhookConfigEntity): WebhookConfigEntity {
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            WebhookSql.INSERT_CONFIG,
            entity.id,
            entity.clientId,
            entity.url,
            entity.secret,
            entity.events,
            entity.isActive,
            entity.maxRetries,
            entity.retryDelay,
            now,
            now
        )

        return entity.copy(createdAt = now, updatedAt = now)
    }

    fun findConfigById(id: UUID): WebhookConfigEntity? {
        return try {
            jdbcTemplate.queryForObject(WebhookSql.FIND_CONFIG_BY_ID, configMapper, id)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findActiveConfigs(): List<WebhookConfigEntity> {
        return jdbcTemplate.query(WebhookSql.FIND_ALL_ACTIVE_CONFIGS, configMapper)
    }

    fun softDeleteConfig(id: UUID) {
        jdbcTemplate.update(WebhookSql.DELETE_CONFIG, id)
    }

    fun saveDelivery(entity: WebhookDeliveryEntity): WebhookDeliveryEntity {
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            WebhookSql.INSERT_DELIVERY,
            entity.id,
            entity.webhookConfigId,
            entity.bankslipId,
            entity.eventType.name,
            entity.payload,
            entity.url,
            entity.statusCode,
            entity.responseBody,
            entity.attemptNumber,
            entity.deliveredAt,
            entity.errorMessage,
            now
        )

        return entity.copy(createdAt = now)
    }

    fun findDeliveriesByConfigId(configId: UUID): List<WebhookDeliveryEntity> {
        return jdbcTemplate.query(WebhookSql.FIND_DELIVERIES_BY_CONFIG, deliveryMapper, configId)
    }

    fun findFailedDeliveries(maxAttempts: Int): List<WebhookDeliveryEntity> {
        return jdbcTemplate.query(WebhookSql.FIND_FAILED_DELIVERIES, deliveryMapper, maxAttempts)
    }
}

class WebhookConfigEntityRowMapper : RowMapper<WebhookConfigEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): WebhookConfigEntity {
        return WebhookConfigEntity(
            id = UUID.fromString(rs.getString("id")),
            clientId = rs.getString("client_id"),
            url = rs.getString("url"),
            secret = rs.getString("secret"),
            events = rs.getString("events"),
            isActive = rs.getBoolean("is_active"),
            maxRetries = rs.getInt("max_retries"),
            retryDelay = rs.getInt("retry_delay"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}

class WebhookDeliveryEntityRowMapper : RowMapper<WebhookDeliveryEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): WebhookDeliveryEntity {
        return WebhookDeliveryEntity(
            id = UUID.fromString(rs.getString("id")),
            webhookConfigId = UUID.fromString(rs.getString("webhook_config_id")),
            bankslipId = UUID.fromString(rs.getString("bankslip_id")),
            eventType = br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType.valueOf(rs.getString("event_type")),
            payload = rs.getString("payload"),
            url = rs.getString("url"),
            statusCode = rs.getObject("status_code") as? Int,
            responseBody = rs.getString("response_body"),
            attemptNumber = rs.getInt("attempt_number"),
            deliveredAt = rs.getTimestamp("delivered_at")?.toLocalDateTime(),
            errorMessage = rs.getString("error_message"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime()
        )
    }
}

