package br.com.misterstorm.bankslipgenerator.adapter.output.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc.JdbcBankSlipRepository
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class BankSlipRepositoryAdapter(
    private val jdbcRepository: JdbcBankSlipRepository
) : BankSlipRepository {

    override suspend fun save(bankSlip: BankSlip): Either<DomainError, BankSlip> {
        return try {
            val saved = jdbcRepository.save(bankSlip)
            saved.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to save bankSlip", e).left()
        }
    }

    override suspend fun findById(id: UUID): Either<DomainError, BankSlip> {
        val bankSlip = jdbcRepository.findById(id)
        return if (bankSlip != null) {
            bankSlip.right()
        } else {
            DomainError.BankSlipNotFound(id.toString()).left()
        }
    }

    override suspend fun findByDocumentNumber(documentNumber: String): Either<DomainError, BankSlip> {
        val bankSlip = jdbcRepository.findByDocumentNumber(documentNumber)
        return if (bankSlip != null) {
            bankSlip.right()
        } else {
            DomainError.BankSlipNotFound(documentNumber).left()
        }
    }

    override suspend fun findByBarcode(barcode: String): Either<DomainError, BankSlip> {
        val bankSlip = jdbcRepository.findByBarcode(barcode)
        return if (bankSlip != null) {
            bankSlip.right()
        } else {
            DomainError.BankSlipNotFound(barcode).left()
        }
    }

    override suspend fun findByStatus(
        status: BankSlipStatus,
        page: Int,
        size: Int
    ): Either<DomainError, List<BankSlip>> {
        return try {
            val bankSlips = jdbcRepository.findByStatus(status, page, size)
            bankSlips.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find bankSlips by status", e).left()
        }
    }

    override suspend fun findByDueDateBetween(
        startDate: LocalDate,
        endDate: LocalDate
    ): Either<DomainError, List<BankSlip>> {
        return try {
            val bankSlips = jdbcRepository.findByDueDateBetween(startDate, endDate)
            bankSlips.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find bankSlips by due date", e).left()
        }
    }

    override suspend fun findByPayerDocumentNumber(
        documentNumber: String,
        page: Int,
        size: Int
    ): Either<DomainError, List<BankSlip>> {
        return try {
            val bankSlips = jdbcRepository.findByPayerDocumentNumber(documentNumber, page, size)
            bankSlips.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to find bankSlips by payer", e).left()
        }
    }

    override suspend fun update(bankSlip: BankSlip): Either<DomainError, BankSlip> {
        return try {
            val updated = jdbcRepository.update(bankSlip)
            updated.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to update bankSlip", e).left()
        }
    }

    override suspend fun delete(id: UUID): Either<DomainError, Unit> {
        return try {
            jdbcRepository.softDelete(id)
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to delete bankSlip", e).left()
        }
    }

    override suspend fun softDelete(id: UUID): Either<DomainError, Unit> {
        return try {
            jdbcRepository.softDelete(id)
            Unit.right()
        } catch (e: Exception) {
            DomainError.UnexpectedError("Failed to soft delete bankSlip", e).left()
        }
    }
}
