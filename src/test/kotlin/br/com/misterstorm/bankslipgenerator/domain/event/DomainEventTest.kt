package br.com.misterstorm.bankslipgenerator.domain.event

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for Domain Events
 */
class DomainEventTest {

    @Test
    fun `ensure BankSlipCreated event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val bankCode = "001"
        val amount = "100.00"
        val dueDate = "2026-03-14"
        val payerDocument = "12345678900"

        // Act
        val event = BankSlipEvent.BankSlipCreated(
            aggregateId = aggregateId,
            bankCode = bankCode,
            amount = amount,
            dueDate = dueDate,
            payerDocument = payerDocument
        )

        // Assert
        assertNotNull(event.eventId)
        assertNotNull(event.occurredOn)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(bankCode, event.bankCode)
        assertEquals(amount, event.amount)
        assertEquals(dueDate, event.dueDate)
        assertEquals(payerDocument, event.payerDocument)
    }

    @Test
    fun `ensure BankSlipRegistered event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val registrationType = "ONLINE"
        val registrationId = "REG-123"

        // Act
        val event = BankSlipEvent.BankSlipRegistered(
            aggregateId = aggregateId,
            registrationType = registrationType,
            registrationId = registrationId
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(registrationType, event.registrationType)
        assertEquals(registrationId, event.registrationId)
    }

    @Test
    fun `ensure BankSlipPaid event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val paidAmount = "100.00"
        val paymentDate = "2026-02-14T10:30:00"

        // Act
        val event = BankSlipEvent.BankSlipPaid(
            aggregateId = aggregateId,
            paidAmount = paidAmount,
            paymentDate = paymentDate
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(paidAmount, event.paidAmount)
        assertEquals(paymentDate, event.paymentDate)
    }

    @Test
    fun `ensure BankSlipCancelled event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val reason = "Customer request"

        // Act
        val event = BankSlipEvent.BankSlipCancelled(
            aggregateId = aggregateId,
            reason = reason
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(reason, event.reason)
    }

    @Test
    fun `ensure BankSlipExpired event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()

        // Act
        val event = BankSlipEvent.BankSlipExpired(
            aggregateId = aggregateId
        )

        // Assert
        assertNotNull(event.eventId)
        assertNotNull(event.occurredOn)
        assertEquals(aggregateId, event.aggregateId)
    }

    @Test
    fun `ensure BankSlipRegistrationFailed event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val errorMessage = "Bank service unavailable"
        val errorCode = "ERR_001"

        // Act
        val event = BankSlipEvent.BankSlipRegistrationFailed(
            aggregateId = aggregateId,
            errorMessage = errorMessage,
            errorCode = errorCode
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(errorMessage, event.errorMessage)
        assertEquals(errorCode, event.errorCode)
    }

    @Test
    fun `ensure WebhookTriggered event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val webhookUrl = "https://example.com/webhook"
        val eventType = "BANKSLIP_CREATED"

        // Act
        val event = WebhookEvent.WebhookTriggered(
            aggregateId = aggregateId,
            webhookUrl = webhookUrl,
            eventType = eventType
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(webhookUrl, event.webhookUrl)
        assertEquals(eventType, event.eventType)
    }

    @Test
    fun `ensure WebhookDelivered event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val webhookUrl = "https://example.com/webhook"
        val statusCode = 200

        // Act
        val event = WebhookEvent.WebhookDelivered(
            aggregateId = aggregateId,
            webhookUrl = webhookUrl,
            statusCode = statusCode
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(webhookUrl, event.webhookUrl)
        assertEquals(statusCode, event.statusCode)
    }

    @Test
    fun `ensure WebhookFailed event has correct properties`() {
        // Arrange
        val aggregateId = UUID.randomUUID()
        val webhookUrl = "https://example.com/webhook"
        val errorMessage = "Connection timeout"
        val attemptNumber = 3

        // Act
        val event = WebhookEvent.WebhookFailed(
            aggregateId = aggregateId,
            webhookUrl = webhookUrl,
            errorMessage = errorMessage,
            attemptNumber = attemptNumber
        )

        // Assert
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(webhookUrl, event.webhookUrl)
        assertEquals(errorMessage, event.errorMessage)
        assertEquals(attemptNumber, event.attemptNumber)
    }

    @Test
    fun `ensure event occurredOn is close to current time`() {
        // Arrange & Act
        val before = LocalDateTime.now()
        val event = BankSlipEvent.BankSlipCreated(
            aggregateId = UUID.randomUUID(),
            bankCode = "001",
            amount = "100.00",
            dueDate = "2026-03-14",
            payerDocument = "12345678900"
        )
        val after = LocalDateTime.now()

        // Assert
        assertTrue(event.occurredOn >= before)
        assertTrue(event.occurredOn <= after)
    }
}

