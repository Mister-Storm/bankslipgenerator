package br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto

import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request DTO for creating webhook configuration
 * JSON will be in kebab-case automatically
 */
data class CreateWebhookConfigRequest(
    val clientId: String,
    val url: String,
    val secret: String,
    val events: List<WebhookEventType>,
    val maxRetries: Int = 3,
    val retryDelay: Int = 5000
)

/**
 * Response DTO for webhook configuration
 */
data class WebhookConfigResponse(
    val id: UUID,
    val clientId: String,
    val url: String,
    val events: List<WebhookEventType>,
    val isActive: Boolean,
    val maxRetries: Int,
    val retryDelay: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Response DTO for webhook delivery
 */
data class WebhookDeliveryResponse(
    val id: UUID,
    val webhookConfigId: UUID,
    val bankslipId: UUID,
    val eventType: WebhookEventType,
    val url: String,
    val statusCode: Int?,
    val attemptNumber: Int,
    val deliveredAt: LocalDateTime?,
    val errorMessage: String?,
    val createdAt: LocalDateTime
)

// Extension functions for mapping
fun WebhookConfig.toResponse(): WebhookConfigResponse = WebhookConfigResponse(
    id = id,
    clientId = clientId,
    url = url,
    events = events,
    isActive = isActive,
    maxRetries = maxRetries,
    retryDelay = retryDelay,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WebhookDelivery.toResponse(): WebhookDeliveryResponse = WebhookDeliveryResponse(
    id = id,
    webhookConfigId = webhookConfigId,
    bankslipId = bankslipId,
    eventType = eventType,
    url = url,
    statusCode = statusCode,
    attemptNumber = attemptNumber,
    deliveredAt = deliveredAt,
    errorMessage = errorMessage,
    createdAt = createdAt
)

