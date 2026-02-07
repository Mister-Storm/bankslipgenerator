package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

/**
 * RowMapper for Bankslip entity
 */
class BankslipRowMapper : RowMapper<Bankslip> {

    private val json = Json { ignoreUnknownKeys = true }

    override fun mapRow(rs: ResultSet, rowNum: Int): Bankslip {
        return Bankslip(
            id = UUID.fromString(rs.getString("id")),
            bankCode = rs.getString("bank_code"),
            documentNumber = rs.getString("document_number"),
            barcode = rs.getString("barcode"),
            digitableLine = rs.getString("digitable_line"),
            amount = rs.getBigDecimal("amount"),
            dueDate = rs.getDate("due_date").toLocalDate(),
            issueDate = rs.getDate("issue_date").toLocalDate(),
            status = BankslipStatus.valueOf(rs.getString("status")),
            payer = Payer(
                name = rs.getString("payer_name"),
                documentNumber = rs.getString("payer_document_number"),
                address = Address(
                    street = rs.getString("payer_street"),
                    number = rs.getString("payer_number"),
                    neighborhood = rs.getString("payer_neighborhood"),
                    city = rs.getString("payer_city"),
                    state = rs.getString("payer_state"),
                    zipCode = rs.getString("payer_zip_code")
                )
            ),
            beneficiary = Beneficiary(
                name = rs.getString("beneficiary_name"),
                documentNumber = rs.getString("beneficiary_document_number"),
                address = Address(
                    street = rs.getString("beneficiary_street"),
                    number = rs.getString("beneficiary_number"),
                    neighborhood = rs.getString("beneficiary_neighborhood"),
                    city = rs.getString("beneficiary_city"),
                    state = rs.getString("beneficiary_state"),
                    zipCode = rs.getString("beneficiary_zip_code")
                ),
                agencyNumber = rs.getString("beneficiary_agency_number"),
                accountNumber = rs.getString("beneficiary_account_number"),
                accountDigit = rs.getString("beneficiary_account_digit")
            ),
            instructions = parseJsonList(rs.getString("instructions")),
            discount = parseDiscount(rs.getString("discount")),
            fine = parseFine(rs.getString("fine")),
            interest = parseInterest(rs.getString("interest")),
            paymentDate = rs.getTimestamp("payment_date")?.toLocalDateTime(),
            paidAmount = rs.getBigDecimal("paid_amount"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }

    private fun parseJsonList(jsonString: String?): List<String> {
        return if (jsonString != null && jsonString != "null") {
            json.decodeFromString<List<String>>(jsonString)
        } else {
            emptyList()
        }
    }

    private fun parseDiscount(jsonString: String?): Discount? {
        return if (jsonString != null && jsonString != "null") {
            json.decodeFromString<Discount>(jsonString)
        } else {
            null
        }
    }

    private fun parseFine(jsonString: String?): Fine? {
        return if (jsonString != null && jsonString != "null") {
            json.decodeFromString<Fine>(jsonString)
        } else {
            null
        }
    }

    private fun parseInterest(jsonString: String?): Interest? {
        return if (jsonString != null && jsonString != "null") {
            json.decodeFromString<Interest>(jsonString)
        } else {
            null
        }
    }
}

/**
 * Helper functions to prepare data for insertion/update
 */
object BankslipMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun prepareInsertParams(bankslip: Bankslip): Array<Any?> {
        return arrayOf(
            bankslip.id,
            bankslip.bankCode,
            bankslip.documentNumber,
            bankslip.barcode,
            bankslip.digitableLine,
            bankslip.amount,
            bankslip.dueDate,
            bankslip.issueDate,
            bankslip.status.name,
            bankslip.payer.name,
            bankslip.payer.documentNumber,
            bankslip.payer.address.street,
            bankslip.payer.address.number,
            bankslip.payer.address.neighborhood,
            bankslip.payer.address.city,
            bankslip.payer.address.state,
            bankslip.payer.address.zipCode,
            bankslip.beneficiary.name,
            bankslip.beneficiary.documentNumber,
            bankslip.beneficiary.address.street,
            bankslip.beneficiary.address.number,
            bankslip.beneficiary.address.neighborhood,
            bankslip.beneficiary.address.city,
            bankslip.beneficiary.address.state,
            bankslip.beneficiary.address.zipCode,
            bankslip.beneficiary.agencyNumber,
            bankslip.beneficiary.accountNumber,
            bankslip.beneficiary.accountDigit,
            json.encodeToString(bankslip.instructions),
            bankslip.discount?.let { json.encodeToString(it) },
            bankslip.fine?.let { json.encodeToString(it) },
            bankslip.interest?.let { json.encodeToString(it) },
            bankslip.paymentDate,
            bankslip.paidAmount,
            bankslip.createdAt,
            bankslip.updatedAt,
            null // deleted_at
        )
    }

    fun prepareUpdateParams(bankslip: Bankslip): Array<Any?> {
        return arrayOf(
            bankslip.bankCode,
            bankslip.documentNumber,
            bankslip.barcode,
            bankslip.digitableLine,
            bankslip.amount,
            bankslip.dueDate,
            bankslip.issueDate,
            bankslip.status.name,
            bankslip.payer.name,
            bankslip.payer.documentNumber,
            bankslip.payer.address.street,
            bankslip.payer.address.number,
            bankslip.payer.address.neighborhood,
            bankslip.payer.address.city,
            bankslip.payer.address.state,
            bankslip.payer.address.zipCode,
            bankslip.beneficiary.name,
            bankslip.beneficiary.documentNumber,
            bankslip.beneficiary.address.street,
            bankslip.beneficiary.address.number,
            bankslip.beneficiary.address.neighborhood,
            bankslip.beneficiary.address.city,
            bankslip.beneficiary.address.state,
            bankslip.beneficiary.address.zipCode,
            bankslip.beneficiary.agencyNumber,
            bankslip.beneficiary.accountNumber,
            bankslip.beneficiary.accountDigit,
            json.encodeToString(bankslip.instructions),
            bankslip.discount?.let { json.encodeToString(it) },
            bankslip.fine?.let { json.encodeToString(it) },
            bankslip.interest?.let { json.encodeToString(it) },
            bankslip.paymentDate,
            bankslip.paidAmount,
            bankslip.updatedAt,
            bankslip.id
        )
    }
}

