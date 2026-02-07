package br.com.misterstorm.bankslipgenerator.application.usecase.bankconfiguration

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.*
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.*

/**
 * Use case for creating a new bank configuration
 */
class CreateBankConfigurationUseCase(
    private val bankConfigurationRepository: BankConfigurationRepository,
    logger: Logger
) : UseCase<CreateBankConfigurationUseCase.Input, BankConfiguration>(logger) {

    data class Input(
        val bankCode: String,
        val bankName: String,
        val layoutConfiguration: LayoutConfiguration,
        val cnabConfiguration: CnabConfiguration,
        val validationRules: List<ValidationRule> = emptyList()
    )

    override suspend fun execute(input: Input): Either<DomainError, BankConfiguration> {
        val bankConfiguration = BankConfiguration(
            id = UUID.randomUUID(),
            bankCode = input.bankCode,
            bankName = input.bankName,
            layoutConfiguration = input.layoutConfiguration,
            cnabConfiguration = input.cnabConfiguration,
            validationRules = input.validationRules,
            isActive = true
        )

        return bankConfigurationRepository.save(bankConfiguration)
    }
}

