package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.BankslipEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.Bankslip
import br.com.misterstorm.bankslipgenerator.domain.model.BankslipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Use case for registering a payment for a bankslip
 */
class PayBankslipUseCase(
    private val bankslipRepository: BankslipRepository,
    private val eventPublisher: DomainEventPublisher,
    logger: Logger
) : UseCase<PayBankslipUseCase.Input, Bankslip>(logger) {

    data class Input(
        val bankslipId: UUID,
        val paymentDate: LocalDateTime,
        val paidAmount: BigDecimal
    )

    override suspend fun execute(input: Input): Either<DomainError, Bankslip> {
        val bankslip = bankslipRepository.findById(input.bankslipId)
            .fold({ return it.left() }, { it })

        // Check if already paid
        if (bankslip.status == BankslipStatus.PAID) {
            return DomainError.BankslipAlreadyPaid(input.bankslipId.toString()).left()
        }

        // Check if cancelled
        if (bankslip.status == BankslipStatus.CANCELLED) {
            return DomainError.BankslipAlreadyCancelled(input.bankslipId.toString()).left()
        }

        // Validate status transition
        if (!bankslip.canTransitionTo(BankslipStatus.PAID)) {
            return DomainError.InvalidStatusTransition(
                from = bankslip.status.name,
                to = BankslipStatus.PAID.name
            ).left()
        }

        // Update bankslip
        val updatedBankslip = bankslip.copy(
            status = BankslipStatus.PAID,
            paymentDate = input.paymentDate,
            paidAmount = input.paidAmount,
            updatedAt = LocalDateTime.now()
        )

        return bankslipRepository.update(updatedBankslip)
            .onRight { paid ->
                // Publish domain event
                val event = BankslipEvent.BankslipPaid(
                    aggregateId = paid.id,
                    paidAmount = paid.paidAmount.toString(),
                    paymentDate = paid.paymentDate.toString()
                )
                eventPublisher.publish(event)
            }
    }
}

