package br.com.misterstorm.bankslipgenerator.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Webhook configuration for status notifications
 */
data class WebhookConfig(
    val id: UUID,
    val clientId: String,
    val url: String,
    val secret: String, // For HMAC signature
    val events: List<WebhookEventType>,
    val isActive: Boolean = true,
    val maxRetries: Int = 3,
    val retryDelay: Int = 5000, // milliseconds
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Webhook event types
 */
enum class WebhookEventType {
    BANKSLIP_CREATED,
    BANKSLIP_REGISTERED,
    BANKSLIP_PAID,
    BANKSLIP_CANCELLED,
    BANKSLIP_EXPIRED,
    BANKSLIP_REGISTRATION_FAILED
}

/**
 * Webhook delivery record
 */
data class WebhookDelivery(
    val id: UUID,
    val webhookConfigId: UUID,
    val bankslipId: UUID,
    val eventType: WebhookEventType,
    val payload: String,
    val url: String,
    val statusCode: Int? = null,
    val responseBody: String? = null,
    val attemptNumber: Int = 1,
    val deliveredAt: LocalDateTime? = null,
    val errorMessage: String? = null,
    val createdAt: LocalDateTime
)

