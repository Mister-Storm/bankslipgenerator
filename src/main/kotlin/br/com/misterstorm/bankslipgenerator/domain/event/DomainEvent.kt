package br.com.misterstorm.bankslipgenerator.domain.event

import java.time.LocalDateTime
import java.util.UUID

/**
 * Base interface for all domain events
 */
interface DomainEvent {
    val eventId: UUID
    val occurredOn: LocalDateTime
    val aggregateId: UUID
}

/**
 * BankSlip domain events
 */
sealed class BankSlipEvent : DomainEvent {

    data class BankSlipCreated(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val bankCode: String,
        val amount: String,
        val dueDate: String,
        val payerDocument: String
    ) : BankSlipEvent()

    data class BankSlipRegistered(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val registrationType: String,
        val registrationId: String? = null
    ) : BankSlipEvent()

    data class BankSlipPaid(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val paidAmount: String,
        val paymentDate: String
    ) : BankSlipEvent()

    data class BankSlipCancelled(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val reason: String? = null
    ) : BankSlipEvent()

    data class BankSlipRegistrationFailed(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val errorMessage: String,
        val errorCode: String? = null
    ) : BankSlipEvent()

    data class BankSlipExpired(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID
    ) : BankSlipEvent()
}

/**
 * Webhook domain events
 */
sealed class WebhookEvent : DomainEvent {
    
    data class WebhookTriggered(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val webhookUrl: String,
        val eventType: String
    ) : WebhookEvent()
    
    data class WebhookDelivered(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val webhookUrl: String,
        val statusCode: Int
    ) : WebhookEvent()
    
    data class WebhookFailed(
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredOn: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: UUID,
        val webhookUrl: String,
        val errorMessage: String,
        val attemptNumber: Int
    ) : WebhookEvent()
}

