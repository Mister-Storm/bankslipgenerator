package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.jdbc

import br.com.misterstorm.bankslipgenerator.domain.model.Address
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.model.Beneficiary
import br.com.misterstorm.bankslipgenerator.domain.model.Discount
import br.com.misterstorm.bankslipgenerator.domain.model.Fine
import br.com.misterstorm.bankslipgenerator.domain.model.Interest
import br.com.misterstorm.bankslipgenerator.domain.model.Payer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.UUID

/**
 * RowMapper for BankSlip entity
 */
@Component
class BankSlipRowMapper : RowMapper<BankSlip> {

    private val json = Json { ignoreUnknownKeys = true }

    override fun mapRow(rs: ResultSet, rowNum: Int): BankSlip {
        return BankSlip(
            id = UUID.fromString(rs.getString("id")),
            bankCode = rs.getString("bank_code"),
            documentNumber = rs.getString("document_number"),
            barcode = rs.getString("barcode"),
            digitableLine = rs.getString("digitable_line"),
            amount = rs.getBigDecimal("amount"),
            dueDate = rs.getDate("due_date").toLocalDate(),
            issueDate = rs.getDate("issue_date").toLocalDate(),
            status = BankSlipStatus.valueOf(rs.getString("status")),
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
object BankSlipMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun prepareInsertParams(bankSlip: BankSlip): Array<Any?> {
        return arrayOf(
            bankSlip.id,
            bankSlip.bankCode,
            bankSlip.documentNumber,
            bankSlip.barcode,
            bankSlip.digitableLine,
            bankSlip.amount,
            bankSlip.dueDate,
            bankSlip.issueDate,
            bankSlip.status.name,
            bankSlip.payer.name,
            bankSlip.payer.documentNumber,
            bankSlip.payer.address.street,
            bankSlip.payer.address.number,
            bankSlip.payer.address.neighborhood,
            bankSlip.payer.address.city,
            bankSlip.payer.address.state,
            bankSlip.payer.address.zipCode,
            bankSlip.beneficiary.name,
            bankSlip.beneficiary.documentNumber,
            bankSlip.beneficiary.address.street,
            bankSlip.beneficiary.address.number,
            bankSlip.beneficiary.address.neighborhood,
            bankSlip.beneficiary.address.city,
            bankSlip.beneficiary.address.state,
            bankSlip.beneficiary.address.zipCode,
            bankSlip.beneficiary.agencyNumber,
            bankSlip.beneficiary.accountNumber,
            bankSlip.beneficiary.accountDigit,
            json.encodeToString(bankSlip.instructions),
            bankSlip.discount?.let { json.encodeToString(it) },
            bankSlip.fine?.let { json.encodeToString(it) },
            bankSlip.interest?.let { json.encodeToString(it) },
            bankSlip.paymentDate,
            bankSlip.paidAmount,
            bankSlip.createdAt,
            bankSlip.updatedAt,
            null // deleted_at
        )
    }

    fun prepareUpdateParams(bankSlip: BankSlip): Array<Any?> {
        return arrayOf(
            bankSlip.bankCode,
            bankSlip.documentNumber,
            bankSlip.barcode,
            bankSlip.digitableLine,
            bankSlip.amount,
            bankSlip.dueDate,
            bankSlip.issueDate,
            bankSlip.status.name,
            bankSlip.payer.name,
            bankSlip.payer.documentNumber,
            bankSlip.payer.address.street,
            bankSlip.payer.address.number,
            bankSlip.payer.address.neighborhood,
            bankSlip.payer.address.city,
            bankSlip.payer.address.state,
            bankSlip.payer.address.zipCode,
            bankSlip.beneficiary.name,
            bankSlip.beneficiary.documentNumber,
            bankSlip.beneficiary.address.street,
            bankSlip.beneficiary.address.number,
            bankSlip.beneficiary.address.neighborhood,
            bankSlip.beneficiary.address.city,
            bankSlip.beneficiary.address.state,
            bankSlip.beneficiary.address.zipCode,
            bankSlip.beneficiary.agencyNumber,
            bankSlip.beneficiary.accountNumber,
            bankSlip.beneficiary.accountDigit,
            json.encodeToString(bankSlip.instructions),
            bankSlip.discount?.let { json.encodeToString(it) },
            bankSlip.fine?.let { json.encodeToString(it) },
            bankSlip.interest?.let { json.encodeToString(it) },
            bankSlip.paymentDate,
            bankSlip.paidAmount,
            bankSlip.updatedAt,
            bankSlip.id
        )
    }
}
