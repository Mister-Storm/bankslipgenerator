package br.com.misterstorm.bankslipgenerator.adapter.output.webhook

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Service for delivering webhooks with retry logic and HMAC signature
 */
@Component
class WebhookDeliveryService(
    private val webClientBuilder: WebClient.Builder,
    private val logger: Logger
) {

    @CircuitBreaker(name = "webhook", fallbackMethod = "deliveryFallback")
    suspend fun deliver(
        webhookConfig: WebhookConfig,
        bankslipId: UUID,
        eventType: WebhookEventType,
        payload: Map<String, Any>
    ): Either<DomainError, WebhookDelivery> {

        val payloadJson = kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            kotlinx.serialization.json.JsonObject(payload.mapValues {
                kotlinx.serialization.json.JsonPrimitive(it.value.toString())
            })
        )

        var lastError: String? = null
        var statusCode: Int? = null
        var responseBody: String? = null

        for (attempt in 1..webhookConfig.maxRetries) {
            try {
                logger.info(
                    "Attempting webhook delivery",
                    "webhookId" to webhookConfig.id.toString(),
                    "url" to webhookConfig.url,
                    "attempt" to attempt,
                    "eventType" to eventType.name
                )

                // Generate HMAC signature
                val signature = generateHmacSignature(payloadJson, webhookConfig.secret)

                val client = webClientBuilder
                    .baseUrl(webhookConfig.url)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("X-Webhook-Signature", signature)
                    .defaultHeader("X-Webhook-Event", eventType.name)
                    .defaultHeader("X-Webhook-Attempt", attempt.toString())
                    .build()

                val response = client.post()
                    .bodyValue(payloadJson)
                    .retrieve()
                    .toEntity(String::class.java)
                    .awaitSingle()

                statusCode = response.statusCode.value()
                responseBody = response.body

                logger.info(
                    "Webhook delivered successfully",
                    "webhookId" to webhookConfig.id.toString(),
                    "statusCode" to statusCode,
                    "attempt" to attempt
                )

                return WebhookDelivery(
                    id = UUID.randomUUID(),
                    webhookConfigId = webhookConfig.id,
                    bankslipId = bankslipId,
                    eventType = eventType,
                    payload = payloadJson,
                    url = webhookConfig.url,
                    statusCode = statusCode,
                    responseBody = responseBody,
                    attemptNumber = attempt,
                    deliveredAt = LocalDateTime.now(),
                    createdAt = LocalDateTime.now()
                ).right()

            } catch (e: Exception) {
                lastError = e.message
                logger.warn(
                    "Webhook delivery attempt failed",
                    "webhookId" to webhookConfig.id.toString(),
                    "attempt" to attempt,
                    "error" to lastError.orEmpty()
                )

                if (attempt < webhookConfig.maxRetries) {
                    val delayMs = webhookConfig.retryDelay * attempt // Exponential backoff
                    logger.debug("Retrying webhook delivery in ${delayMs}ms")
                    delay(delayMs.toLong())
                }
            }
        }

        // All attempts failed
        logger.error(
            "Webhook delivery failed after all attempts",
            "webhookId" to webhookConfig.id.toString(),
            "maxRetries" to webhookConfig.maxRetries
        )

        return WebhookDelivery(
            id = UUID.randomUUID(),
            webhookConfigId = webhookConfig.id,
            bankslipId = bankslipId,
            eventType = eventType,
            payload = payloadJson,
            url = webhookConfig.url,
            statusCode = statusCode,
            responseBody = responseBody,
            attemptNumber = webhookConfig.maxRetries,
            errorMessage = lastError,
            createdAt = LocalDateTime.now()
        ).right()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun deliveryFallback(
        webhookConfig: WebhookConfig,
        bankslipId: UUID,
        eventType: WebhookEventType,
        payload: Map<String, Any>,
        ex: Exception
    ): Either<DomainError, WebhookDelivery> {
        logger.warn("Circuit breaker activated for webhook delivery")
        return DomainError.UnexpectedError("Webhook delivery circuit breaker open", ex).left()
    }

    private fun generateHmacSignature(payload: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        mac.init(secretKey)
        val hmacBytes = mac.doFinal(payload.toByteArray())
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }
}

