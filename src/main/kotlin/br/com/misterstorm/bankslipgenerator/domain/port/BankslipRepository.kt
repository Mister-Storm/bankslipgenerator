package br.com.misterstorm.bankslipgenerator.domain.port

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import java.time.LocalDate
import java.util.*

/**
 * Repository port for Bankslip persistence
 */
interface BankslipRepository {
    suspend fun save(bankslip: Bankslip): Either<DomainError, Bankslip>
    suspend fun findById(id: UUID): Either<DomainError, Bankslip>
    suspend fun findByDocumentNumber(documentNumber: String): Either<DomainError, Bankslip>
    suspend fun findByBarcode(barcode: String): Either<DomainError, Bankslip>
    suspend fun findByStatus(status: BankslipStatus, page: Int, size: Int): Either<DomainError, List<Bankslip>>
    suspend fun findByDueDateBetween(startDate: LocalDate, endDate: LocalDate): Either<DomainError, List<Bankslip>>
    suspend fun findByPayerDocumentNumber(documentNumber: String, page: Int, size: Int): Either<DomainError, List<Bankslip>>
    suspend fun update(bankslip: Bankslip): Either<DomainError, Bankslip>
    suspend fun delete(id: UUID): Either<DomainError, Unit>
    suspend fun softDelete(id: UUID): Either<DomainError, Unit>
}

