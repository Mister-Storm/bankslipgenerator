package br.com.misterstorm.bankslipgenerator.application.usecase.webhook

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.port.WebhookRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.UUID

/**
 * Use case for deleting webhook configuration
 */
class DeleteWebhookConfigUseCase(
    private val webhookRepository: WebhookRepository,
    logger: Logger
) : UseCase<UUID, Unit>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, Unit> {
        return webhookRepository.deleteConfig(input)
    }
}

