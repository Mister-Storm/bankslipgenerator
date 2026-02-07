package br.com.misterstorm.bankslipgenerator.application.usecase.webhook

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.adapter.output.webhook.WebhookDeliveryService
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookDelivery
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.*

/**
 * Use case for testing webhook delivery
 */
class TestWebhookUseCase(
    private val webhookRepository: WebhookRepository,
    private val webhookDeliveryService: WebhookDeliveryService,
    logger: Logger
) : UseCase<UUID, WebhookDelivery>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, WebhookDelivery> {
        val webhookConfig = webhookRepository.findConfigById(input)
            .fold({ return it.left() }, { it })

        val testPayload = mapOf(
            "event-type" to "TEST",
            "webhook-id" to webhookConfig.id.toString(),
            "test" to true,
            "message" to "This is a test webhook delivery",
            "timestamp" to java.time.LocalDateTime.now().toString()
        )

        return webhookDeliveryService.deliver(
            webhookConfig = webhookConfig,
            bankslipId = UUID.randomUUID(), // Fake bankslip ID for test
            eventType = WebhookEventType.BANKSLIP_CREATED, // Arbitrary event type
            payload = testPayload
        )
    }
}

