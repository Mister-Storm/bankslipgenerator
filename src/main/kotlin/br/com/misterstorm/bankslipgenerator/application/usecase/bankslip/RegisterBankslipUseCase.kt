package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDateTime
import java.util.*

/**
 * Use case for registering a bankslip with the bank
 */
class RegisterBankslipUseCase(
    private val bankslipRepository: BankslipRepository,
    logger: Logger
) : UseCase<UUID, Bankslip>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, Bankslip> {
        val bankslip = bankslipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Check current status
        if (bankslip.status != BankslipStatus.CREATED) {
            return DomainError.InvalidStatusTransition(
                from = bankslip.status.name,
                to = BankslipStatus.REGISTERED.name
            ).left()
        }

        // Validate status transition
        if (!bankslip.canTransitionTo(BankslipStatus.REGISTERED)) {
            return DomainError.InvalidStatusTransition(
                from = bankslip.status.name,
                to = BankslipStatus.REGISTERED.name
            ).left()
        }

        // Update status to registered
        val updatedBankslip = bankslip.copy(
            status = BankslipStatus.REGISTERED,
            updatedAt = LocalDateTime.now()
        )

        return bankslipRepository.update(updatedBankslip)
    }
}

