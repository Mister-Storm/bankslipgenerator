package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.domain.port.FileStorageService
import br.com.misterstorm.bankslipgenerator.domain.port.PdfGeneratorService
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.*

/**
 * Use case for generating PDF for a bankslip
 */
class GenerateBankslipPdfUseCase(
    private val bankslipRepository: BankslipRepository,
    private val bankConfigurationRepository: BankConfigurationRepository,
    private val pdfGeneratorService: PdfGeneratorService,
    private val fileStorageService: FileStorageService,
    logger: Logger
) : UseCase<UUID, String>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, String> {
        val bankslip = bankslipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Validate bank configuration exists
        val bankConfig = bankConfigurationRepository.findByBankCode(bankslip.bankCode)
            .fold({ return it.left() }, { it })

        // Generate PDF
        val pdfContent = pdfGeneratorService.generatePdf(bankslip)
            .fold({ return it.left() }, { it })

        // Upload to storage
        val fileName = "bankslip_${bankslip.id}.pdf"
        return fileStorageService.upload(
            fileName = fileName,
            content = pdfContent,
            contentType = "application/pdf",
            metadata = mapOf(
                "bankslipId" to bankslip.id.toString(),
                "bankCode" to bankslip.bankCode,
                "documentNumber" to bankslip.documentNumber
            )
        )
    }
}

