package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.UUID

/**
 * Use case for soft deleting a BankSlip
 */
class DeleteBankSlipUseCase(
    private val bankSlipRepository: BankSlipRepository,
    logger: Logger
) : UseCase<UUID, Unit>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, Unit> {
        val bankSlip = bankSlipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Check if already deleted
        if (bankSlip.isDeleted()) {
            return DomainError.BankSlipAlreadyCancelled(input.toString()).left()
        }

        // Perform soft delete
        return bankSlipRepository.softDelete(input)
    }
}
