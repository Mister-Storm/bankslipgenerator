package br.com.misterstorm.bankslipgenerator.application.usecase.cnab

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import br.com.misterstorm.bankslipgenerator.domain.model.CnabFile
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabFileRepository
import br.com.misterstorm.bankslipgenerator.domain.port.CnabService
import br.com.misterstorm.bankslipgenerator.domain.port.FileStorageService
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.time.LocalDate

/**
 * Use case for generating CNAB remittance file
 */
class GenerateRemittanceFileUseCase(
    private val bankslipRepository: BankslipRepository,
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
        // Validate bank configuration
        val bankConfig = bankConfigurationRepository.findByBankCode(input.bankCode)
            .fold({ return it.left() }, { it })

        // Get bankslips to be registered
        val bankslips = bankslipRepository.findByDueDateBetween(input.startDate, input.endDate)
            .fold({ return it.left() }, { it })
            .filter { it.status == BankslipStatus.CREATED && !it.isDeleted() }

        if (bankslips.isEmpty()) {
            return DomainError.ValidationFailed(
                violations = listOf("No bankslips found for the given period")
            ).left()
        }

        // Generate CNAB file
        val cnabFile = cnabService.generateRemittanceFile(
            bankslips = bankslips,
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

