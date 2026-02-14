package br.com.misterstorm.bankslipgenerator.application.usecase.cnab

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabFileRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabService
import br.com.misterstorm.bankslipgenerator.domain.port.FileStorageService
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDate

/**
 * Use case for generating CNAB remittance file
 */
class GenerateRemittanceFileUseCase(
    private val bankSlipRepository: BankSlipRepository,
    private val bankConfigurationRepository: BankConfigurationRepository,
    private val cnabService: CnabService,
    private val cnabFileRepository: CnabFileRepository,
    private val fileStorageService: FileStorageService,
    logger: Logger
) : UseCase<GenerateRemittanceFileUseCase.Input, CnabFile>(logger) {

    data class Input(
        val bankCode: String,
        val version: CnabVersion,
        val startDate: LocalDate,
        val endDate: LocalDate
    )

    override suspend fun execute(input: Input): Either<DomainError, CnabFile> {
        // Validate bank configuration exists
        bankConfigurationRepository.findByBankCode(input.bankCode)
            .fold({ return it.left() }, { it })

        // Get bankSlips to be registered
        val bankSlips = bankSlipRepository.findByDueDateBetween(input.startDate, input.endDate)
            .fold({ return it.left() }, { it })
            .filter { it.status == BankSlipStatus.CREATED && !it.isDeleted() }

        if (bankSlips.isEmpty()) {
            return DomainError.ValidationFailed(
                violations = listOf("No bankSlips found for the given period")
            ).left()
        }

        // Generate CNAB file
        val cnabFile = cnabService.generateRemittanceFile(
            bankSlips = bankSlips,
            bankCode = input.bankCode,
            version = input.version
        ).fold({ return it.left() }, { it })

        // Upload to storage
        val fileUrl = fileStorageService.upload(
            fileName = cnabFile.fileName,
            content = cnabFile.fileContent.toByteArray(),
            contentType = "text/plain",
            metadata = mapOf(
                "bankCode" to input.bankCode,
                "version" to input.version.name,
                "recordCount" to cnabFile.totalRecords.toString()
            )
        ).fold({ return it.left() }, { it })

        // Save CNAB file record with URL
        val cnabFileWithUrl = cnabFile.copy(fileUrl = fileUrl)
        return cnabFileRepository.save(cnabFileWithUrl)
    }
}

