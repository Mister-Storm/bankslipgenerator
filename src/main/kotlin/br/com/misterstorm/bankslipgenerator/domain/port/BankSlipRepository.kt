package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import java.time.LocalDate
import java.util.UUID

/**
 * Repository port for BankSlip persistence
 */
interface BankSlipRepository {
    suspend fun save(bankSlip: BankSlip): Either<DomainError, BankSlip>
    suspend fun findById(id: UUID): Either<DomainError, BankSlip>
    suspend fun findByDocumentNumber(documentNumber: String): Either<DomainError, BankSlip>
    suspend fun findByBarcode(barcode: String): Either<DomainError, BankSlip>
    suspend fun findByStatus(status: BankSlipStatus, page: Int, size: Int): Either<DomainError, List<BankSlip>>
    suspend fun findByDueDateBetween(startDate: LocalDate, endDate: LocalDate): Either<DomainError, List<BankSlip>>
    suspend fun findByPayerDocumentNumber(documentNumber: String, page: Int, size: Int): Either<DomainError, List<BankSlip>>
    suspend fun update(bankSlip: BankSlip): Either<DomainError, BankSlip>
    suspend fun delete(id: UUID): Either<DomainError, Unit>
    suspend fun softDelete(id: UUID): Either<DomainError, Unit>
}
