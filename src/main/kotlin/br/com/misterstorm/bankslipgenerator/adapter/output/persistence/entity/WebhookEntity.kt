package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity

import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.*

/**
 * Entity for webhook configuration persistence (JDBC - no JPA annotations)
 */
data class WebhookConfigEntity(
    val id: UUID = UUID.randomUUID(),
    val clientId: String,
    val url: String,
    val secret: String,
    val events: String, // JSON array of event types
    val isActive: Boolean = true,
    val maxRetries: Int = 3,
    val retryDelay: Int = 5000, // milliseconds
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)

/**
 * Entity for webhook delivery persistence (JDBC - no JPA annotations)
 */
data class WebhookDeliveryEntity(
    val id: UUID = UUID.randomUUID(),
    val webhookConfigId: UUID,
    val bankslipId: UUID,
    val eventType: WebhookEventType,
    val payload: String, // JSON
    val url: String,
    val statusCode: Int?,
    val responseBody: String?,
    val attemptNumber: Int = 1,
    val deliveredAt: LocalDateTime?,
    val errorMessage: String?,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Extension functions for mapping between domain and entity
fun WebhookConfigEntity.toDomain(): WebhookConfig {
    val eventList = Json.decodeFromString<List<String>>(this.events)
        .map { WebhookEventType.valueOf(it) }

    return WebhookConfig(
        id = this.id,
        clientId = this.clientId,
        url = this.url,
        secret = this.secret,
        events = eventList,
        isActive = this.isActive,
        maxRetries = this.maxRetries,
        retryDelay = this.retryDelay,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun WebhookConfig.toEntity(): WebhookConfigEntity {
    val eventNames: List<String> = this.events.map { it.name }
    val eventsJson = Json.encodeToString(ListSerializer(String.serializer()), eventNames)

    return WebhookConfigEntity(
        id = this.id,
        clientId = this.clientId,
        url = this.url,
        secret = this.secret,
        events = eventsJson,
        isActive = this.isActive,
        maxRetries = this.maxRetries,
        retryDelay = this.retryDelay,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun WebhookDeliveryEntity.toDomain(): WebhookDelivery {
    return WebhookDelivery(
        id = this.id,
        webhookConfigId = this.webhookConfigId,
        bankslipId = this.bankslipId,
        eventType = this.eventType,
        payload = this.payload,
        url = this.url,
        statusCode = this.statusCode,
        responseBody = this.responseBody,
        attemptNumber = this.attemptNumber,
        deliveredAt = this.deliveredAt,
        errorMessage = this.errorMessage,
        createdAt = this.createdAt
    )
}

fun WebhookDelivery.toEntity(): WebhookDeliveryEntity {
    return WebhookDeliveryEntity(
        id = this.id,
        webhookConfigId = this.webhookConfigId,
        bankslipId = this.bankslipId,
        eventType = this.eventType,
        payload = this.payload,
        url = this.url,
        statusCode = this.statusCode,
        responseBody = this.responseBody,
        attemptNumber = this.attemptNumber,
        deliveredAt = this.deliveredAt,
        errorMessage = this.errorMessage,
        createdAt = this.createdAt
    )
}
