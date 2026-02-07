package br.com.misterstorm.bankslipgenerator.application.usecase.webhook

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.WebhookConfig
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.*

/**
 * Use case for retrieving webhook configuration
 */
class GetWebhookConfigUseCase(
    private val webhookRepository: WebhookRepository,
    logger: Logger
) : UseCase<UUID, WebhookConfig>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, WebhookConfig> {
        return webhookRepository.findConfigById(input)
    }
}

