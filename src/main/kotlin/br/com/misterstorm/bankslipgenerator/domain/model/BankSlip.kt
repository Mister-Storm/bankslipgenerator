package br.com.misterstorm.bankslipgenerator.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * BankSlip Status representing its lifecycle
 */
enum class BankSlipStatus {
    CREATED,
    REGISTERED,
    PAID,
    CANCELLED,
    EXPIRED
}

/**
 * BankSlip domain entity
 */
data class BankSlip(
    val id: UUID,
    val bankCode: String,
    val documentNumber: String, // Nosso Número
    val barcode: String,
    val digitableLine: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val issueDate: LocalDate,
    val status: BankSlipStatus,
    val payer: Payer,
    val beneficiary: Beneficiary,
    val instructions: List<String> = emptyList(),
    val paymentDate: LocalDateTime? = null,
    val paidAmount: BigDecimal? = null,
    val discount: Discount? = null,
    val fine: Fine? = null,
    val interest: Interest? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime? = null
) {
    fun isDeleted(): Boolean = deletedAt != null

    fun canTransitionTo(newStatus: BankSlipStatus): Boolean {
        return when (status) {
            BankSlipStatus.CREATED -> newStatus in listOf(
                BankSlipStatus.REGISTERED,
                BankSlipStatus.CANCELLED,
                BankSlipStatus.EXPIRED
            )
            BankSlipStatus.REGISTERED -> newStatus in listOf(
                BankSlipStatus.PAID,
                BankSlipStatus.CANCELLED,
                BankSlipStatus.EXPIRED
            )
            BankSlipStatus.PAID -> false
            BankSlipStatus.CANCELLED -> false
            BankSlipStatus.EXPIRED -> newStatus == BankSlipStatus.PAID
        }
    }

    fun isExpired(): Boolean = LocalDate.now().isAfter(dueDate) && status != BankSlipStatus.PAID
}

/**
 * Payer information
 */
data class Payer(
    val name: String,
    val documentNumber: String, // CPF/CNPJ
    val address: Address
)

/**
 * Beneficiary information
 */
data class Beneficiary(
    val name: String,
    val documentNumber: String, // CPF/CNPJ
    val address: Address,
    val agencyNumber: String,
    val accountNumber: String,
    val accountDigit: String
)

/**
 * Address information
 */
data class Address(
    val street: String,
    val number: String,
    val complement: String? = null,
    val neighborhood: String,
    val city: String,
    val state: String,
    val zipCode: String
)

/**
 * Discount configuration
 */
data class Discount(
    val type: DiscountType,
    val value: BigDecimal,
    val limitDate: LocalDate? = null
)

enum class DiscountType {
    FIXED,
    PERCENTAGE
}

/**
 * Fine configuration
 */
data class Fine(
    val type: FineType,
    val value: BigDecimal,
    val startDate: LocalDate? = null
)

enum class FineType {
    FIXED,
    PERCENTAGE
}

/**
 * Interest configuration
 */
data class Interest(
    val type: InterestType,
    val value: BigDecimal // Daily rate for DAILY type, monthly for MONTHLY
)

enum class InterestType {
    DAILY,
    MONTHLY
}

