package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.UUID
class WebhookConfigRowMapper : RowMapper<WebhookConfig> {
    private val json = Json { ignoreUnknownKeys = true }
    override fun mapRow(rs: ResultSet, rowNum: Int): WebhookConfig {
        return WebhookConfig(
            id = UUID.fromString(rs.getString("id")),
            clientId = rs.getString("client_id"),
            url = rs.getString("url"),
            secret = rs.getString("secret"),
            events = json.decodeFromString<List<WebhookEventType>>(rs.getString("events")),
            isActive = rs.getBoolean("is_active"),
            maxRetries = rs.getInt("max_retries"),
            retryDelay = rs.getInt("retry_delay"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}
class WebhookDeliveryRowMapper : RowMapper<WebhookDelivery> {
    override fun mapRow(rs: ResultSet, rowNum: Int): WebhookDelivery {
        return WebhookDelivery(
            id = UUID.fromString(rs.getString("id")),
            webhookConfigId = UUID.fromString(rs.getString("webhook_config_id")),
            bankslipId = UUID.fromString(rs.getString("bankslip_id")),
            eventType = WebhookEventType.valueOf(rs.getString("event_type")),
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
object WebhookMapper {
    private val json = Json { ignoreUnknownKeys = true }
    fun prepareConfigInsert(config: WebhookConfig) = arrayOf<Any>(
        config.id, config.clientId, config.url, config.secret,
        json.encodeToString(config.events), config.isActive,
        config.maxRetries, config.retryDelay, config.createdAt, config.updatedAt
    )
    fun prepareDeliveryInsert(delivery: WebhookDelivery) = arrayOf<Any?>(
        delivery.id, delivery.webhookConfigId, delivery.bankslipId,
        delivery.eventType.name, delivery.payload, delivery.url,
        delivery.statusCode, delivery.responseBody, delivery.attemptNumber,
        delivery.deliveredAt, delivery.errorMessage, delivery.createdAt
    )
}
