package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.UUID

/**
 * Use case for retrieving a BankSlip by ID
 */
class GetBankSlipUseCase(
    private val bankSlipRepository: BankSlipRepository,
    logger: Logger
) : UseCase<UUID, BankSlip>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, BankSlip> {
        return bankSlipRepository.findById(input)
    }
}
