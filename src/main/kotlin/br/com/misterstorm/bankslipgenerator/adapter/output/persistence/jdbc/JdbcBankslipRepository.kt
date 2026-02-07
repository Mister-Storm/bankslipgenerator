package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.BankslipEntity
import br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity.toEntity
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
class JdbcBankslipRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val rowMapper: BankslipRowMapper
) {
    fun save(bankslip: Bankslip): Bankslip {
        val entity = bankslip.toEntity()
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            BankslipSql.INSERT,
            entity.id,
            entity.bankCode,
            entity.documentNumber,
            entity.barcode,
            entity.digitableLine,
            entity.amount,
            entity.dueDate,
            entity.issueDate,
            entity.status.name,
            entity.payerName,
            entity.payerDocumentNumber,
            entity.payerStreet,
            entity.payerNumber,
            entity.payerComplement,
            entity.payerNeighborhood,
            entity.payerCity,
            entity.payerState,
            entity.payerZipCode,
            entity.beneficiaryName,
            entity.beneficiaryDocumentNumber,
            entity.beneficiaryAgencyNumber,
            entity.beneficiaryAccountNumber,
            entity.beneficiaryAccountDigit,
            entity.beneficiaryStreet,
            entity.beneficiaryNumber,
            entity.beneficiaryComplement,
            entity.beneficiaryNeighborhood,
            entity.beneficiaryCity,
            entity.beneficiaryState,
            entity.beneficiaryZipCode,
            entity.paymentDate,
            entity.paidAmount,
            entity.discountType,
            entity.discountValue,
            entity.discountLimitDate,
            entity.fineType,
            entity.fineValue,
            entity.fineStartDate,
            entity.interestType,
            entity.interestValue,
            now,
            now
        )

        return bankslip.copy(createdAt = now, updatedAt = now)
    }

    fun update(bankslip: Bankslip): Bankslip {
        val entity = bankslip.toEntity()
        val now = LocalDateTime.now()

        jdbcTemplate.update(
            BankslipSql.UPDATE,
            entity.status.name,
            entity.paymentDate,
            entity.paidAmount,
            now,
            entity.id
        )

        return bankslip.copy(updatedAt = now)
    }

    fun findById(id: UUID): Bankslip? {
        return try {
            jdbcTemplate.queryForObject(BankslipSql.FIND_BY_ID, rowMapper, id)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findByDocumentNumber(documentNumber: String): Bankslip? {
        return try {
            jdbcTemplate.queryForObject(BankslipSql.FIND_BY_DOCUMENT_NUMBER, rowMapper, documentNumber)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findByBarcode(barcode: String): Bankslip? {
        return try {
            jdbcTemplate.queryForObject(BankslipSql.FIND_BY_BARCODE, rowMapper, barcode)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun findByStatus(status: BankslipStatus, page: Int, size: Int): List<Bankslip> {
        return jdbcTemplate.query(
            "${BankslipSql.FIND_BY_STATUS} LIMIT ? OFFSET ?",
            rowMapper,
            status.name,
            size,
            page * size
        )
    }

    fun findByDueDateBetween(startDate: LocalDate, endDate: LocalDate): List<Bankslip> {
        return jdbcTemplate.query(BankslipSql.FIND_BY_DUE_DATE_BETWEEN, rowMapper, startDate, endDate)
    }

    fun findByPayerDocumentNumber(documentNumber: String, page: Int, size: Int): List<Bankslip> {
        return jdbcTemplate.query(
            "${BankslipSql.FIND_BY_PAYER_DOCUMENT} LIMIT ? OFFSET ?",
            rowMapper,
            documentNumber,
            size,
            page * size
        )
    }

    fun softDelete(id: UUID) {
        jdbcTemplate.update(BankslipSql.SOFT_DELETE, LocalDateTime.now(), id)
    }
}

