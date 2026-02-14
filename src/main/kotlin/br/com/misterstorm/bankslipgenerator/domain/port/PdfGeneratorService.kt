package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip

/**
 * Service port for PDF generation
 */
interface PdfGeneratorService {
    suspend fun generatePdf(bankSlip: BankSlip): Either<DomainError, ByteArray>
}
