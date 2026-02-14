package br.com.misterstorm.bankslipgenerator.application.usecase.webhook

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookEventType
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDateTime
import java.util.UUID

/**
 * Use case for creating webhook configuration
 */
class CreateWebhookConfigUseCase(
    private val webhookRepository: WebhookRepository,
    logger: Logger
) : UseCase<CreateWebhookConfigUseCase.Input, WebhookConfig>(logger) {

    data class Input(
        val clientId: String,
        val url: String,
        val secret: String,
        val events: List<WebhookEventType>,
        val maxRetries: Int = 3,
        val retryDelay: Int = 5000
    )

    override suspend fun execute(input: Input): Either<DomainError, WebhookConfig> {
        // Validate URL format
        if (!input.url.startsWith("http://") && !input.url.startsWith("https://")) {
            return Either.Left(
                DomainError.ValidationFailed(
                    violations = listOf("URL must start with http:// or https://")
                )
            )
        }

        // Validate events not empty
        if (input.events.isEmpty()) {
            return Either.Left(
                DomainError.ValidationFailed(
                    violations = listOf("At least one event type must be specified")
                )
            )
        }

        val now = LocalDateTime.now()
        val webhookConfig = WebhookConfig(
            id = UUID.randomUUID(),
            clientId = input.clientId,
            url = input.url,
            secret = input.secret,
            events = input.events,
            isActive = true,
            maxRetries = input.maxRetries,
            retryDelay = input.retryDelay,
            createdAt = now,
            updatedAt = now
        )

        return webhookRepository.saveConfig(webhookConfig)
    }
}

