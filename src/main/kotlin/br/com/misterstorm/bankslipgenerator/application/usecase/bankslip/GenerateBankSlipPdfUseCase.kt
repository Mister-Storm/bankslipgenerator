package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.domain.port.FileStorageService
import br.com.misterstorm.bankslipgenerator.domain.port.PdfGeneratorService
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.util.UUID

/**
 * Use case for generating PDF for a BankSlip
 */
class GenerateBankSlipPdfUseCase(
    private val bankSlipRepository: BankSlipRepository,
    private val bankConfigurationRepository: BankConfigurationRepository,
    private val pdfGeneratorService: PdfGeneratorService,
    private val fileStorageService: FileStorageService,
    logger: Logger
) : UseCase<UUID, String>(logger) {

    override suspend fun execute(input: UUID): Either<DomainError, String> {
        val bankSlip = bankSlipRepository.findById(input)
            .fold({ return it.left() }, { it })

        // Validate bank configuration exists
        bankConfigurationRepository.findByBankCode(bankSlip.bankCode)
            .fold({ return it.left() }, { it })

        // Generate PDF
        val pdfContent = pdfGeneratorService.generatePdf(bankSlip)
            .fold({ return it.left() }, { it })

        // Upload to storage
        val fileName = "bankslip_${bankSlip.id}.pdf"
        return fileStorageService.upload(
            fileName = fileName,
            content = pdfContent,
            contentType = "application/pdf",
            metadata = mapOf(
                "bankSlipId" to bankSlip.id.toString(),
                "bankCode" to bankSlip.bankCode,
                "documentNumber" to bankSlip.documentNumber
            )
        )
    }
}
