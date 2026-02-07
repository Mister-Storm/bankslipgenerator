package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.BankslipEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.*
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Use case for creating a new bankslip
 */
class CreateBankslipUseCase(
    private val bankslipRepository: BankslipRepository,
    private val bankConfigurationRepository: BankConfigurationRepository,
    private val eventPublisher: DomainEventPublisher,
    logger: Logger
) : UseCase<CreateBankslipUseCase.Input, Bankslip>(logger) {

    data class Input(
        val bankCode: String,
        val amount: BigDecimal,
        val dueDate: LocalDate,
        val payer: Payer,
        val beneficiary: Beneficiary,
        val instructions: List<String> = emptyList(),
        val discount: Discount? = null,
        val fine: Fine? = null,
        val interest: Interest? = null
    )

    override suspend fun execute(input: Input): Either<DomainError, Bankslip> {
        // Validate bank configuration exists
        val bankConfig = bankConfigurationRepository.findByBankCode(input.bankCode).fold(
            { error -> return error.left() },
            { config -> config }
        )

        // Validate due date
        if (input.dueDate.isBefore(LocalDate.now())) {
            return DomainError.InvalidDueDate(input.dueDate.toString()).left()
        }

        // Validate amount
        if (input.amount <= BigDecimal.ZERO) {
            return DomainError.InvalidAmount(input.amount.toString()).left()
        }

        // Generate document number (nosso número)
        val documentNumber = generateDocumentNumber(input.bankCode)

        // Generate barcode and digitable line
        val barcode = generateBarcode(input, documentNumber)
        val digitableLine = generateDigitableLine(barcode)

        // Create bankslip
        val now = LocalDateTime.now()
        val bankslip = Bankslip(
            id = UUID.randomUUID(),
            bankCode = input.bankCode,
            documentNumber = documentNumber,
            barcode = barcode,
            digitableLine = digitableLine,
            amount = input.amount,
            dueDate = input.dueDate,
            issueDate = LocalDate.now(),
            status = BankslipStatus.CREATED,
            payer = input.payer,
            beneficiary = input.beneficiary,
            instructions = input.instructions,
            discount = input.discount,
            fine = input.fine,
            interest = input.interest,
            createdAt = now,
            updatedAt = now
        )

        return bankslipRepository.save(bankslip)
            .onRight { savedBankslip ->
                // Publish domain event
                val event = BankslipEvent.BankslipCreated(
                    aggregateId = savedBankslip.id,
                    bankCode = savedBankslip.bankCode,
                    amount = savedBankslip.amount.toString(),
                    dueDate = savedBankslip.dueDate.toString(),
                    payerDocument = savedBankslip.payer.documentNumber
                )
                eventPublisher.publish(event)
            }
    }

    private fun generateDocumentNumber(bankCode: String): String {
        // Simple implementation - in production, this should follow bank-specific rules
        val timestamp = System.currentTimeMillis()
        return "$bankCode${timestamp % 10000000000}"
    }

    private fun generateBarcode(input: Input, documentNumber: String): String {
        // Simplified barcode generation
        // Real implementation should follow FEBRABAN specifications
        val bankCode = input.bankCode.padStart(3, '0')
        val currencyCode = "9" // Real
        val dv = calculateBarcodeVerificationDigit(input)
        val dueDateFactor = calculateDueDateFactor(input.dueDate)
        val amount = input.amount.multiply(BigDecimal(100)).toLong().toString().padStart(10, '0')
        val freeField = documentNumber.padEnd(25, '0').take(25)

        return "$bankCode$currencyCode$dv$dueDateFactor$amount$freeField"
    }

    private fun calculateBarcodeVerificationDigit(input: Input): String {
        // Simplified - real implementation uses module 11
        return "1"
    }

    private fun calculateDueDateFactor(dueDate: LocalDate): String {
        val baseDate = LocalDate.of(1997, 10, 7)
        val days = java.time.temporal.ChronoUnit.DAYS.between(baseDate, dueDate)
        return days.toString().padStart(4, '0')
    }

    private fun generateDigitableLine(barcode: String): String {
        // Simplified digitable line generation
        // Real implementation should follow FEBRABAN specifications
        if (barcode.length < 44) return barcode

        val field1 = "${barcode.substring(0, 4)}${barcode.substring(19, 24)}"
        val field2 = barcode.substring(24, 34)
        val field3 = barcode.substring(34, 44)
        val field4 = barcode.substring(4, 5)
        val field5 = barcode.substring(5, 19)

        return "$field1.$field2.$field3 $field4 $field5"
    }
}

