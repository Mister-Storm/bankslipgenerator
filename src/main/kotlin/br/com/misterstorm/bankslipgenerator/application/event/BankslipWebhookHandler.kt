package br.com.misterstorm.bankslipgenerator.application.event

import br.com.misterstorm.bankslipgenerator.adapter.output.webhook.WebhookDeliveryService
import br.com.misterstorm.bankslipgenerator.domain.event.BankslipEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventHandler
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Event handler that listens to bankslip events and triggers webhooks
 */
@Component
class BankslipWebhookHandler(
    private val webhookRepository: WebhookRepository,
    private val webhookDeliveryService: WebhookDeliveryService,
    private val logger: Logger
) : DomainEventHandler<BankslipEvent> {

    @Async
    @EventListener
    override suspend fun handle(event: BankslipEvent) {
        if (!canHandle(event)) return

        logger.info(
            "Handling bankslip event for webhook delivery",
            "eventType" to event::class.simpleName.orEmpty(),
            "aggregateId" to event.aggregateId.toString()
        )

        val webhookEventType = mapToWebhookEventType(event) ?: run {
            logger.warn("No webhook event type mapping for ${event::class.simpleName}")
            return
        }

        // Get all active webhooks
        val webhookConfigs = webhookRepository.findActiveConfigs().fold(
            { error ->
                logger.error("Failed to load webhook configurations", "error" to error.message)
                return
            },
            { it }
        )

        // Filter webhooks that are subscribed to this event type
        val subscribedWebhooks = webhookConfigs.filter { config ->
            webhookEventType in config.events
        }

        logger.info(
            "Found subscribed webhooks",
            "count" to subscribedWebhooks.size,
            "eventType" to webhookEventType.name
        )

        // Deliver webhooks asynchronously
        subscribedWebhooks.forEach { webhookConfig ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val payload = buildPayload(event)
                    
                    val delivery = webhookDeliveryService.deliver(
                        webhookConfig = webhookConfig,
                        bankslipId = event.aggregateId,
                        eventType = webhookEventType,
                        payload = payload
                    ).fold(
                        { error ->
                            logger.error("Webhook delivery failed", "error" to error.message)
                            return@launch
                        },
                        { it }
                    )

                    // Save delivery record
                    webhookRepository.saveDelivery(delivery)
                        .onLeft { error ->
                            logger.error("Failed to save webhook delivery record", "error" to error.message)
                        }

                } catch (e: Exception) {
                    logger.error(
                        "Unexpected error during webhook delivery",
                        e,
                        "webhookId" to webhookConfig.id.toString()
                    )
                }
            }
        }
    }

    override fun canHandle(event: DomainEvent): Boolean {
        return event is BankslipEvent
    }

    private fun mapToWebhookEventType(event: BankslipEvent): WebhookEventType? {
        return when (event) {
            is BankslipEvent.BankslipCreated -> WebhookEventType.BANKSLIP_CREATED
            is BankslipEvent.BankslipRegistered -> WebhookEventType.BANKSLIP_REGISTERED
            is BankslipEvent.BankslipPaid -> WebhookEventType.BANKSLIP_PAID
            is BankslipEvent.BankslipCancelled -> WebhookEventType.BANKSLIP_CANCELLED
            is BankslipEvent.BankslipExpired -> WebhookEventType.BANKSLIP_EXPIRED
            is BankslipEvent.BankslipRegistrationFailed -> WebhookEventType.BANKSLIP_REGISTRATION_FAILED
        }
    }

    private fun buildPayload(event: BankslipEvent): Map<String, Any> {
        return when (event) {
            is BankslipEvent.BankslipCreated -> mapOf(
                "eventType" to "BANKSLIP_CREATED",
                "bankslipId" to event.aggregateId.toString(),
                "bankCode" to event.bankCode,
                "amount" to event.amount,
                "dueDate" to event.dueDate,
                "payerDocument" to event.payerDocument,
                "timestamp" to event.occurredOn.toString()
            )
            is BankslipEvent.BankslipRegistered -> mapOf(
                "eventType" to "BANKSLIP_REGISTERED",
                "bankslipId" to event.aggregateId.toString(),
                "registrationType" to event.registrationType,
                "registrationId" to (event.registrationId ?: ""),
                "timestamp" to event.occurredOn.toString()
            )
            is BankslipEvent.BankslipPaid -> mapOf(
                "eventType" to "BANKSLIP_PAID",
                "bankslipId" to event.aggregateId.toString(),
                "paidAmount" to event.paidAmount,
                "paymentDate" to event.paymentDate,
                "timestamp" to event.occurredOn.toString()
            )
            is BankslipEvent.BankslipCancelled -> mapOf(
                "eventType" to "BANKSLIP_CANCELLED",
                "bankslipId" to event.aggregateId.toString(),
                "reason" to (event.reason ?: ""),
                "timestamp" to event.occurredOn.toString()
            )
            is BankslipEvent.BankslipExpired -> mapOf(
                "eventType" to "BANKSLIP_EXPIRED",
                "bankslipId" to event.aggregateId.toString(),
                "timestamp" to event.occurredOn.toString()
            )
            is BankslipEvent.BankslipRegistrationFailed -> mapOf(
                "eventType" to "BANKSLIP_REGISTRATION_FAILED",
                "bankslipId" to event.aggregateId.toString(),
                "errorMessage" to event.errorMessage,
                "errorCode" to (event.errorCode ?: ""),
                "timestamp" to event.occurredOn.toString()
            )
        }
    }
}

