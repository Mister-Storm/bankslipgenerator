package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDateTime
import java.util.UUID

/**
 * Use case for registering a BankSlip with the bank
 */
class RegisterBankSlipUseCase(
    private val bankSlipRepository: BankSlipRepository,
    logger: Logger
) : UseCase<UUID, BankSlip>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, BankSlip> {
        val bankSlip = bankSlipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Check current status
        if (bankSlip.status != BankSlipStatus.CREATED) {
            return DomainError.InvalidStatusTransition(
                from = bankSlip.status.name,
                to = BankSlipStatus.REGISTERED.name
            ).left()
        }

        // Validate status transition
        if (!bankSlip.canTransitionTo(BankSlipStatus.REGISTERED)) {
            return DomainError.InvalidStatusTransition(
                from = bankSlip.status.name,
                to = BankSlipStatus.REGISTERED.name
            ).left()
        }

        // Update status to registered
        val updatedBankSlip = bankSlip.copy(
            status = BankSlipStatus.REGISTERED,
            updatedAt = LocalDateTime.now()
        )

        return bankSlipRepository.update(updatedBankSlip)
    }
}
