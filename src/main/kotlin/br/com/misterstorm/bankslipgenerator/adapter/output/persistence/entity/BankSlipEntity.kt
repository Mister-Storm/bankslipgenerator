package br.com.misterstorm.bankslipgenerator.adapter.output.persistence.entity

import br.com.misterstorm.bankslipgenerator.domain.model.Address
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.model.Beneficiary
import br.com.misterstorm.bankslipgenerator.domain.model.Discount
import br.com.misterstorm.bankslipgenerator.domain.model.DiscountType
import br.com.misterstorm.bankslipgenerator.domain.model.Fine
import br.com.misterstorm.bankslipgenerator.domain.model.FineType
import br.com.misterstorm.bankslipgenerator.domain.model.Interest
import br.com.misterstorm.bankslipgenerator.domain.model.InterestType
import br.com.misterstorm.bankslipgenerator.domain.model.Payer
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity for BankSlip persistence (JDBC - no JPA annotations)
 */
data class BankSlipEntity(
    val id: UUID = UUID.randomUUID(),
    val bankCode: String,
    val documentNumber: String,
    val barcode: String,
    val digitableLine: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val issueDate: LocalDate,
    val status: BankSlipStatus,

    // Payer information
    val payerName: String,
    val payerDocumentNumber: String,
    val payerStreet: String,
    val payerNumber: String,
    val payerComplement: String?,
    val payerNeighborhood: String,
    val payerCity: String,
    val payerState: String,
    val payerZipCode: String,

    // Beneficiary information
    val beneficiaryName: String,
    val beneficiaryDocumentNumber: String,
    val beneficiaryAgencyNumber: String,
    val beneficiaryAccountNumber: String,
    val beneficiaryAccountDigit: String,
    val beneficiaryStreet: String,
    val beneficiaryNumber: String,
    val beneficiaryComplement: String?,
    val beneficiaryNeighborhood: String,
    val beneficiaryCity: String,
    val beneficiaryState: String,
    val beneficiaryZipCode: String,

    // Payment information
    val paymentDate: LocalDateTime?,
    val paidAmount: BigDecimal?,

    // Discount information
    val discountType: String?,
    val discountValue: BigDecimal?,
    val discountLimitDate: LocalDate?,

    // Fine information
    val fineType: String?,
    val fineValue: BigDecimal?,
    val fineStartDate: LocalDate?,

    // Interest information
    val interestType: String?,
    val interestValue: BigDecimal?,

    // Audit fields
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null
)

// Extension functions for mapping between domain and entity
fun BankSlipEntity.toDomain(): BankSlip {
    return BankSlip(
        id = this.id,
        bankCode = this.bankCode,
        documentNumber = this.documentNumber,
        barcode = this.barcode,
        digitableLine = this.digitableLine,
        amount = this.amount,
        dueDate = this.dueDate,
        issueDate = this.issueDate,
        status = this.status,
        payer = Payer(
            name = this.payerName,
            documentNumber = this.payerDocumentNumber,
            address = Address(
                street = this.payerStreet,
                number = this.payerNumber,
                complement = this.payerComplement,
                neighborhood = this.payerNeighborhood,
                city = this.payerCity,
                state = this.payerState,
                zipCode = this.payerZipCode
            )
        ),
        beneficiary = Beneficiary(
            name = this.beneficiaryName,
            documentNumber = this.beneficiaryDocumentNumber,
            address = Address(
                street = this.beneficiaryStreet,
                number = this.beneficiaryNumber,
                complement = this.beneficiaryComplement,
                neighborhood = this.beneficiaryNeighborhood,
                city = this.beneficiaryCity,
                state = this.beneficiaryState,
                zipCode = this.beneficiaryZipCode
            ),
            agencyNumber = this.beneficiaryAgencyNumber,
            accountNumber = this.beneficiaryAccountNumber,
            accountDigit = this.beneficiaryAccountDigit
        ),
        instructions = emptyList(), // Instructions loaded separately
        paymentDate = this.paymentDate,
        paidAmount = this.paidAmount,
        discount = if (this.discountType != null && this.discountValue != null) {
            Discount(
                type = DiscountType.valueOf(this.discountType),
                value = this.discountValue,
                limitDate = this.discountLimitDate
            )
        } else null,
        fine = if (this.fineType != null && this.fineValue != null) {
            Fine(
                type = FineType.valueOf(this.fineType),
                value = this.fineValue,
                startDate = this.fineStartDate
            )
        } else null,
        interest = if (this.interestType != null && this.interestValue != null) {
            Interest(
                type = InterestType.valueOf(this.interestType),
                value = this.interestValue
            )
        } else null,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        deletedAt = this.deletedAt
    )
}

fun BankSlip.toEntity(): BankSlipEntity {
    return BankSlipEntity(
        id = this.id,
        bankCode = this.bankCode,
        documentNumber = this.documentNumber,
        barcode = this.barcode,
        digitableLine = this.digitableLine,
        amount = this.amount,
        dueDate = this.dueDate,
        issueDate = this.issueDate,
        status = this.status,
        payerName = this.payer.name,
        payerDocumentNumber = this.payer.documentNumber,
        payerStreet = this.payer.address.street,
        payerNumber = this.payer.address.number,
        payerComplement = this.payer.address.complement,
        payerNeighborhood = this.payer.address.neighborhood,
        payerCity = this.payer.address.city,
        payerState = this.payer.address.state,
        payerZipCode = this.payer.address.zipCode,
        beneficiaryName = this.beneficiary.name,
        beneficiaryDocumentNumber = this.beneficiary.documentNumber,
        beneficiaryAgencyNumber = this.beneficiary.agencyNumber,
        beneficiaryAccountNumber = this.beneficiary.accountNumber,
        beneficiaryAccountDigit = this.beneficiary.accountDigit,
        beneficiaryStreet = this.beneficiary.address.street,
        beneficiaryNumber = this.beneficiary.address.number,
        beneficiaryComplement = this.beneficiary.address.complement,
        beneficiaryNeighborhood = this.beneficiary.address.neighborhood,
        beneficiaryCity = this.beneficiary.address.city,
        beneficiaryState = this.beneficiary.address.state,
        beneficiaryZipCode = this.beneficiary.address.zipCode,
        paymentDate = this.paymentDate,
        paidAmount = this.paidAmount,
        discountType = this.discount?.type?.name,
        discountValue = this.discount?.value,
        discountLimitDate = this.discount?.limitDate,
        fineType = this.fine?.type?.name,
        fineValue = this.fine?.value,
        fineStartDate = this.fine?.startDate,
        interestType = this.interest?.type?.name,
        interestValue = this.interest?.value,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        deletedAt = this.deletedAt
    )
}
