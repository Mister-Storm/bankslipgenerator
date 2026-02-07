package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.BankslipEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankOnlineRegistrationService
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDateTime
import java.util.*

/**
 * Use case for registering bankslip online with bank
 * Automatically selects the appropriate bank adapter
 */
class RegisterBankslipOnlineUseCase(
    private val bankslipRepository: BankslipRepository,
    private val onlineServices: List<BankOnlineRegistrationService>,
    private val eventPublisher: DomainEventPublisher,
    logger: Logger
) : UseCase<UUID, Bankslip>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, Bankslip> {
        val bankslip = bankslipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Check if already registered
        if (bankslip.status == BankslipStatus.REGISTERED) {
            return Either.Right(bankslip)
        }

        // Find service that supports this bank
        val service = onlineServices.find { it.supports(bankslip.bankCode) }
            ?: return DomainError.BankConfigurationNotFound(
                "No online registration service available for bank ${bankslip.bankCode}"
            ).left()

        // Register with bank
        return service.register(bankslip).fold(
            { error ->
                // Publish failure event
                eventPublisher.publish(
                    BankslipEvent.BankslipRegistrationFailed(
                        aggregateId = bankslip.id,
                        errorMessage = error.message,
                        errorCode = error.details["errorCode"]?.toString()
                    )
                )
                error.left()
            },
            { response ->
                // Update bankslip status
                val updatedBankslip = bankslip.copy(
                    status = BankslipStatus.REGISTERED,
                    updatedAt = LocalDateTime.now()
                )

                bankslipRepository.update(updatedBankslip)
                    .onRight { updated ->
                        // Publish success event
                        eventPublisher.publish(
                            BankslipEvent.BankslipRegistered(
                                aggregateId = updated.id,
                                registrationType = "ONLINE_API",
                                registrationId = response.registrationId
                            )
                        )
                    }
            }
        )
    }
}

