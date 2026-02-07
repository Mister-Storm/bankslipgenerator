package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.*

/**
 * Use case for retrieving a bankslip by ID
 */
class GetBankslipUseCase(
    private val bankslipRepository: BankslipRepository,
    logger: Logger
) : UseCase<UUID, Bankslip>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, Bankslip> {
        return bankslipRepository.findById(input)
    }
}

