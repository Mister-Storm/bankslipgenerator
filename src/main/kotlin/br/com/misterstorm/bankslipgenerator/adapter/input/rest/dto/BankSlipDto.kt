package br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto

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
 * Request DTO for creating a BankSlip
 */
data class CreateBankSlipRequest(
    val bankCode: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val payer: PayerDto,
    val beneficiary: BeneficiaryDto,
    val instructions: List<String> = emptyList(),
    val discount: DiscountDto? = null,
    val fine: FineDto? = null,
    val interest: InterestDto? = null
)

data class PayerDto(
    val name: String,
    val documentNumber: String,
    val address: AddressDto
)

data class BeneficiaryDto(
    val name: String,
    val documentNumber: String,
    val address: AddressDto,
    val agencyNumber: String,
    val accountNumber: String,
    val accountDigit: String
)

data class AddressDto(
    val street: String,
    val number: String,
    val complement: String? = null,
    val neighborhood: String,
    val city: String,
    val state: String,
    val zipCode: String
)

data class DiscountDto(
    val type: DiscountType,
    val value: BigDecimal,
    val limitDate: LocalDate? = null
)

data class FineDto(
    val type: FineType,
    val value: BigDecimal,
    val startDate: LocalDate? = null
)

data class InterestDto(
    val type: InterestType,
    val value: BigDecimal
)

/**
 * Response DTO for BankSlip
 */
data class BankSlipResponse(
    val id: UUID,
    val bankCode: String,
    val documentNumber: String,
    val barcode: String,
    val digitableLine: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val issueDate: LocalDate,
    val status: BankSlipStatus,
    val payer: PayerDto,
    val beneficiary: BeneficiaryDto,
    val instructions: List<String>,
    val paymentDate: LocalDateTime? = null,
    val paidAmount: BigDecimal? = null,
    val discount: DiscountDto? = null,
    val fine: FineDto? = null,
    val interest: InterestDto? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Request DTO for paying a BankSlip
 */
data class PayBankSlipRequest(
    val paymentDate: LocalDateTime,
    val paidAmount: BigDecimal
)

// Extension functions for mapping
fun CreateBankSlipRequest.toPayer(): Payer = Payer(
    name = payer.name,
    documentNumber = payer.documentNumber,
    address = payer.address.toDomain()
)

fun CreateBankSlipRequest.toBeneficiary(): Beneficiary = Beneficiary(
    name = beneficiary.name,
    documentNumber = beneficiary.documentNumber,
    address = beneficiary.address.toDomain(),
    agencyNumber = beneficiary.agencyNumber,
    accountNumber = beneficiary.accountNumber,
    accountDigit = beneficiary.accountDigit
)

fun AddressDto.toDomain(): Address = Address(
    street = street,
    number = number,
    complement = complement,
    neighborhood = neighborhood,
    city = city,
    state = state,
    zipCode = zipCode
)

fun DiscountDto.toDomain(): Discount = Discount(
    type = type,
    value = value,
    limitDate = limitDate
)

fun FineDto.toDomain(): Fine = Fine(
    type = type,
    value = value,
    startDate = startDate
)

fun InterestDto.toDomain(): Interest = Interest(
    type = type,
    value = value
)

fun BankSlip.toResponse(): BankSlipResponse = BankSlipResponse(
    id = id,
    bankCode = bankCode,
    documentNumber = documentNumber,
    barcode = barcode,
    digitableLine = digitableLine,
    amount = amount,
    dueDate = dueDate,
    issueDate = issueDate,
    status = status,
    payer = payer.toDto(),
    beneficiary = beneficiary.toDto(),
    instructions = instructions,
    paymentDate = paymentDate,
    paidAmount = paidAmount,
    discount = discount?.toDto(),
    fine = fine?.toDto(),
    interest = interest?.toDto(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Payer.toDto(): PayerDto = PayerDto(
    name = name,
    documentNumber = documentNumber,
    address = address.toDto()
)

fun Beneficiary.toDto(): BeneficiaryDto = BeneficiaryDto(
    name = name,
    documentNumber = documentNumber,
    address = address.toDto(),
    agencyNumber = agencyNumber,
    accountNumber = accountNumber,
    accountDigit = accountDigit
)

fun Address.toDto(): AddressDto = AddressDto(
    street = street,
    number = number,
    complement = complement,
    neighborhood = neighborhood,
    city = city,
    state = state,
    zipCode = zipCode
)

fun Discount.toDto(): DiscountDto = DiscountDto(
    type = type,
    value = value,
    limitDate = limitDate
)

fun Fine.toDto(): FineDto = FineDto(
    type = type,
    value = value,
    startDate = startDate
)

fun Interest.toDto(): InterestDto = InterestDto(
    type = type,
    value = value
)
