package br.com.misterstorm.bankslipgenerator.application.usecase.cnab

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabFileRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabService
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger

/**
 * Use case for processing CNAB return file
 */
class ProcessReturnFileUseCase(
    private val bankConfigurationRepository: BankConfigurationRepository,
    private val cnabService: CnabService,
    private val cnabFileRepository: CnabFileRepository,
    logger: Logger
) : UseCase<ProcessReturnFileUseCase.Input, CnabFile>(logger) {

    data class Input(
        val bankCode: String,
        val version: CnabVersion,
        val fileContent: String
    )

    override suspend fun execute(input: Input): Either<DomainError, CnabFile> {
        // Validate bank configuration
        val bankConfig = bankConfigurationRepository.findByBankCode(input.bankCode)
            .fold({ return it.left() }, { it })

        // Process CNAB return file
        val cnabFile = cnabService.processReturnFile(
            fileContent = input.fileContent,
            bankCode = input.bankCode,
            version = input.version
        ).fold({ return it.left() }, { it })

        // Save CNAB file record
        return cnabFileRepository.save(cnabFile)
    }
}

