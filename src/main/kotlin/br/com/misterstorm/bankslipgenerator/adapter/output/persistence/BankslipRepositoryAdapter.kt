package br.com.misterstorm.bankslipgenerator.adapter.output.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.*
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.JdbcBankslipRepository
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
class BankslipRepositoryAdapter(
    private val jdbcRepository: JdbcBankslipRepository
) : BankslipRepository {

    override suspend fun save(bankslip: Bankslip): Either<DomainError, Bankslip> {
        return try {
            val saved = jdbcRepository.save(bankslip)
            saved.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to save bankslip", e).left()
        }
    }

    override suspend fun findById(id: UUID): Either<DomainError, Bankslip> {
        val bankslip = jdbcRepository.findById(id)
        return if (bankslip != null) {
            bankslip.right()
        } else {
            DomainError.BankslipNotFound(id.toString()).left()
        }
    }

    override suspend fun findByDocumentNumber(documentNumber: String): Either<DomainError, Bankslip> {
        val bankslip = jdbcRepository.findByDocumentNumber(documentNumber)
        return if (bankslip != null) {
            bankslip.right()
        } else {
            DomainError.BankslipNotFound(documentNumber).left()
        }
    }

    override suspend fun findByBarcode(barcode: String): Either<DomainError, Bankslip> {
        val bankslip = jdbcRepository.findByBarcode(barcode)
        return if (bankslip != null) {
            bankslip.right()
        } else {
            DomainError.BankslipNotFound(barcode).left()
        }
    }

    override suspend fun findByStatus(
        status: BankslipStatus,
        page: Int,
        size: Int
    ): Either<DomainError, List<Bankslip>> {
        return try {
            val bankslips = jdbcRepository.findByStatus(status, page, size)
            bankslips.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find bankslips by status", e).left()
        }
    }

    override suspend fun findByDueDateBetween(
        startDate: LocalDate,
        endDate: LocalDate
    ): Either<DomainError, List<Bankslip>> {
        return try {
            val bankslips = jdbcRepository.findByDueDateBetween(startDate, endDate)
            bankslips.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find bankslips by due date", e).left()
        }
    }

    override suspend fun findByPayerDocumentNumber(
        documentNumber: String,
        page: Int,
        size: Int
    ): Either<DomainError, List<Bankslip>> {
        return try {
            val bankslips = jdbcRepository.findByPayerDocumentNumber(documentNumber, page, size)
            bankslips.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find bankslips by payer", e).left()
        }
    }

    override suspend fun update(bankslip: Bankslip): Either<DomainError, Bankslip> {
        return try {
            val updated = jdbcRepository.update(bankslip)
            updated.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to update bankslip", e).left()
        }
    }

    override suspend fun delete(id: UUID): Either<DomainError, Unit> {
        return try {
            jdbcRepository.softDelete(id)
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to delete bankslip", e).left()
        }
    }

    override suspend fun softDelete(id: UUID): Either<DomainError, Unit> {
        return try {
            jdbcRepository.softDelete(id)
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to soft delete bankslip", e).left()
        }
    }
}
