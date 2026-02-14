package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.BankSlipEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankOnlineRegistrationService
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDateTime
import java.util.UUID

/**
 * Use case for registering BankSlip online with bank
 * Automatically selects the appropriate bank adapter
 */
class RegisterBankSlipOnlineUseCase(
    private val bankSlipRepository: BankSlipRepository,
    private val onlineServices: List<BankOnlineRegistrationService>,
    private val eventPublisher: DomainEventPublisher,
    logger: Logger
) : UseCase<UUID, BankSlip>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, BankSlip> {
        val bankSlip = bankSlipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Check if already registered
        if (bankSlip.status == BankSlipStatus.REGISTERED) {
            return Either.Right(bankSlip)
        }

        // Find service that supports this bank
        val service = onlineServices.find { it.supports(bankSlip.bankCode) }
            ?: return DomainError.BankConfigurationNotFound(
                "No online registration service available for bank ${bankSlip.bankCode}"
            ).left()

        // Register with bank
        return service.register(bankSlip).fold(
            { error ->
                // Publish failure event
                eventPublisher.publish(
                    BankSlipEvent.BankSlipRegistrationFailed(
                        aggregateId = bankSlip.id,
                        errorMessage = error.message,
                        errorCode = error.details["errorCode"]?.toString()
                    )
                )
                error.left()
            },
            { response ->
                // Update bankSlip status
                val updatedBankSlip = bankSlip.copy(
                    status = BankSlipStatus.REGISTERED,
                    updatedAt = LocalDateTime.now()
                )

                bankSlipRepository.update(updatedBankSlip)
                    .onRight { updated ->
                        // Publish success event
                        eventPublisher.publish(
                            BankSlipEvent.BankSlipRegistered(
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
