package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError

/**
 * Service port for barcode generation
 */
interface BarcodeGeneratorService {
    suspend fun generateBarcode(digitableLine: String): Either<DomainError, ByteArray>
}

