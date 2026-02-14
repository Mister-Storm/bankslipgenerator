package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import arrow.core.left
import br.com.misterstorm.bankslipgenerator.application.usecase.UseCase
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.BankSlipEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.Logger
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Use case for registering a payment for a BankSlip
 */
class PayBankSlipUseCase(
    private val bankSlipRepository: BankSlipRepository,
    private val eventPublisher: DomainEventPublisher,
    logger: Logger
) : UseCase<PayBankSlipUseCase.Input, BankSlip>(logger) {

    data class Input(
        val bankSlipId: UUID,
        val paymentDate: LocalDateTime,
        val paidAmount: BigDecimal
    )

    override suspend fun execute(input: Input): Either<DomainError, BankSlip> {
        val bankSlip = bankSlipRepository.findById(input.bankSlipId)
            .fold({ return it.left() }, { it })

        // Check if already paid
        if (bankSlip.status == BankSlipStatus.PAID) {
            return DomainError.BankSlipAlreadyPaid(input.bankSlipId.toString()).left()
        }

        // Check if cancelled
        if (bankSlip.status == BankSlipStatus.CANCELLED) {
            return DomainError.BankSlipAlreadyCancelled(input.bankSlipId.toString()).left()
        }

        // Validate status transition
        if (!bankSlip.canTransitionTo(BankSlipStatus.PAID)) {
            return DomainError.InvalidStatusTransition(
                from = bankSlip.status.name,
                to = BankSlipStatus.PAID.name
            ).left()
        }

        // Update bankSlip
        val updatedBankSlip = bankSlip.copy(
            status = BankSlipStatus.PAID,
            paymentDate = input.paymentDate,
            paidAmount = input.paidAmount,
            updatedAt = LocalDateTime.now()
        )

        return bankSlipRepository.update(updatedBankSlip)
            .onRight { paid ->
                // Publish domain event
                val event = BankSlipEvent.BankSlipPaid(
                    aggregateId = paid.id,
                    paidAmount = paid.paidAmount.toString(),
                    paymentDate = paid.paymentDate.toString()
                )
                eventPublisher.publish(event)
            }
    }
}
