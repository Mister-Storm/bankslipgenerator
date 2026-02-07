package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDateTime
import java.util.*

/**
 * Use case for soft deleting a bankslip
 */
class DeleteBankslipUseCase(
    private val bankslipRepository: BankslipRepository,
    logger: Logger
) : UseCase<UUID, Unit>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, Unit> {
        val bankslip = bankslipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Check if already deleted
        if (bankslip.isDeleted()) {
            return DomainError.BankslipAlreadyCancelled(input.toString()).left()
        }

        // Perform soft delete
        return bankslipRepository.softDelete(input)
    }
}

