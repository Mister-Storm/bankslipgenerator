package br.com.misterstorm.bankslipgenerator.infrastructure.scheduler

import br.com.misterstorm.bankslipgenerator.adapter.output.webhook.WebhookDeliveryService
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import br.com.misterstorm.bankslipgenerator.infrastructure.service.DeadLetterQueueService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Scheduled job to retry failed webhook deliveries
 * Runs every 5 minutes to retry webhooks that failed
 * Sends to DLQ after max attempts exceeded
 */
@Component
class WebhookRetryScheduler(
    private val webhookRepository: WebhookRepository,
    private val webhookDeliveryService: WebhookDeliveryService,
    private val deadLetterQueueService: DeadLetterQueueService,
    private val logger: Logger
) {

    private val json = Json { ignoreUnknownKeys = true }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    fun retryFailedWebhooks() = runBlocking {
        logger.info("Starting webhook retry job")
        
        try {
            val failedDeliveriesResult = webhookRepository
                .findFailedDeliveries(maxAttempts = 3)

            val failedDeliveries = failedDeliveriesResult.fold(
                { error ->
                    logger.error("Failed to load failed deliveries", "error" to error.message)
                    return@runBlocking
                },
                { it }
            )

            if (failedDeliveries.isEmpty()) {
                logger.debug("No failed webhooks to retry")
                return@runBlocking
            }
            
            logger.info(
                "Found failed webhooks to retry",
                "count" to failedDeliveries.size
            )
            
            var successCount = 0
            var failureCount = 0
            var dlqCount = 0

            for (delivery in failedDeliveries) {
                try {
                    // Check if max attempts reached - send to DLQ
                    if (delivery.attemptNumber >= 3) {
                        deadLetterQueueService.addToQueue(
                            entityType = "WEBHOOK_DELIVERY",
                            entityId = delivery.id,
                            payload = delivery.payload,
                            errorMessage = delivery.errorMessage ?: "Max retry attempts exceeded",
                            attempts = delivery.attemptNumber
                        )
                        dlqCount++
                        logger.warn(
                            "Webhook delivery sent to DLQ after max attempts",
                            "deliveryId" to delivery.id.toString()
                        )
                        continue
                    }

                    val config = webhookRepository.findConfigById(delivery.webhookConfigId)
                        .getOrNull() ?: run {
                            logger.warn(
                                "Webhook config not found for delivery",
                                "deliveryId" to delivery.id.toString()
                            )
                            continue
                        }

                    // Parse payload back to map
                    val payload = parsePayload(delivery.payload)

                    // Retry delivery with incremented attempt number
                    val result = webhookDeliveryService.deliver(
                        webhookConfig = config,
                        bankslipId = delivery.bankslipId,
                        eventType = delivery.eventType,
                        payload = payload
                    )

                    // Save updated delivery record
                    result.fold(
                        { error ->
                            failureCount++
                            logger.warn(
                                "Webhook retry failed",
                                "deliveryId" to delivery.id.toString(),
                                "error" to error.message
                            )
                        },
                        { updatedDelivery ->
                            webhookRepository.saveDelivery(updatedDelivery)
                            if (updatedDelivery.deliveredAt != null) {
                                successCount++
                                logger.info(
                                    "Webhook retry successful",
                                    "deliveryId" to delivery.id.toString()
                                )
                            } else {
                                failureCount++
                            }
                        }
                    )
                } catch (e: Exception) {
                    failureCount++
                    logger.error(
                        "Unexpected error during webhook retry",
                        e,
                        "deliveryId" to delivery.id.toString()
                    )
                }
            }
            
            logger.info(
                "Webhook retry job completed",
                "processed" to failedDeliveries.size,
                "successful" to successCount,
                "failed" to failureCount,
                "sentToDLQ" to dlqCount
            )
            
        } catch (e: Exception) {
            logger.error("Webhook retry job failed", e)
        }
    }

    private fun parsePayload(payloadJson: String): Map<String, Any> {
        return try {
            val jsonObject = json.parseToJsonElement(payloadJson).jsonObject
            jsonObject.mapValues { (_, value) -> value.toString().removeSurrounding("\"") }
        } catch (e: Exception) {
            logger.error("Failed to parse webhook payload", e)
            emptyMap()
        }
    }
}

