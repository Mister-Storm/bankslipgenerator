package br.com.misterstorm.bankslipgenerator.domain.error

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for DomainError sealed class
 */
class DomainErrorTest {

    @Test
    fun `ensure BankSlipNotFound has correct message and details`() {
        // Arrange
        val bankSlipId = "123e4567-e89b-12d3-a456-426614174000"

        // Act
        val error = DomainError.BankSlipNotFound(bankSlipId)

        // Assert
        assertEquals("BankSlip not found", error.message)
        assertEquals(bankSlipId, error.details["bankSlipId"])
    }

    @Test
    fun `ensure BankSlipAlreadyPaid has correct message`() {
        // Arrange
        val bankSlipId = "123e4567-e89b-12d3-a456-426614174000"

        // Act
        val error = DomainError.BankSlipAlreadyPaid(bankSlipId)

        // Assert
        assertEquals("BankSlip already paid", error.message)
        assertEquals(bankSlipId, error.details["bankSlipId"])
    }

    @Test
    fun `ensure BankSlipAlreadyCancelled has correct message`() {
        // Arrange
        val bankSlipId = "123e4567-e89b-12d3-a456-426614174000"

        // Act
        val error = DomainError.BankSlipAlreadyCancelled(bankSlipId)

        // Assert
        assertEquals("BankSlip already cancelled", error.message)
        assertEquals(bankSlipId, error.details["bankSlipId"])
    }

    @Test
    fun `ensure InvalidAmount has correct details`() {
        // Arrange
        val amount = "0.00"

        // Act
        val error = DomainError.InvalidAmount(amount)

        // Assert
        assertEquals("Invalid amount", error.message)
        assertEquals(amount, error.details["amount"])
    }

    @Test
    fun `ensure InvalidDueDate has correct details`() {
        // Arrange
        val dueDate = "2020-01-01"

        // Act
        val error = DomainError.InvalidDueDate(dueDate)

        // Assert
        assertEquals("Invalid due date", error.message)
        assertEquals(dueDate, error.details["dueDate"])
    }

    @Test
    fun `ensure BankConfigurationNotFound has correct details`() {
        // Arrange
        val bankCode = "999"

        // Act
        val error = DomainError.BankConfigurationNotFound(bankCode)

        // Assert
        assertEquals("Bank configuration not found", error.message)
        assertEquals(bankCode, error.details["bankCode"])
    }

    @Test
    fun `ensure ValidationFailed contains violations`() {
        // Arrange
        val violations = listOf("Field 'name' is required", "Field 'amount' must be positive")

        // Act
        val error = DomainError.ValidationFailed(violations)

        // Assert
        assertEquals("Validation failed", error.message)
        assertEquals(violations, error.details["violations"])
    }

    @Test
    fun `ensure PdfGenerationFailed has message and details`() {
        // Arrange
        val message = "Template not found"
        val details = mapOf("bankSlipId" to "123")

        // Act
        val error = DomainError.PdfGenerationFailed(message, details)

        // Assert
        assertEquals(message, error.message)
        assertEquals("123", error.details["bankSlipId"])
    }

    @Test
    fun `ensure UnexpectedError wraps exception`() {
        // Arrange
        val message = "Database connection failed"
        val cause = RuntimeException("Connection refused")

        // Act
        val error = DomainError.UnexpectedError(message, cause)

        // Assert
        assertEquals(message, error.message)
        assertNotNull(error.throwable)
        assertEquals(cause, error.throwable)
    }

    @Test
    fun `ensure WebhookConfigNotFound has correct details`() {
        // Arrange
        val webhookId = "webhook-123"

        // Act
        val error = DomainError.WebhookConfigNotFound(webhookId)

        // Assert
        assertEquals("Webhook configuration not found", error.message)
        assertEquals(webhookId, error.details["webhookId"])
    }

    @Test
    fun `ensure InvalidStatusTransition has correct details`() {
        // Arrange
        val from = "CREATED"
        val to = "PAID"

        // Act
        val error = DomainError.InvalidStatusTransition(from, to)

        // Assert
        assertEquals("Invalid status transition", error.message)
        assertEquals(from, error.details["from"])
        assertEquals(to, error.details["to"])
    }

    @Test
    fun `ensure all error types have non-empty message`() {
        // Arrange
        val errors = listOf(
            DomainError.BankSlipNotFound("1"),
            DomainError.BankSlipAlreadyPaid("1"),
            DomainError.BankSlipAlreadyCancelled("1"),
            DomainError.InvalidAmount("0"),
            DomainError.InvalidDueDate("2020-01-01"),
            DomainError.BankConfigurationNotFound("001"),
            DomainError.ValidationFailed(listOf("error")),
            DomainError.PdfGenerationFailed("error"),
            DomainError.UnexpectedError("error"),
            DomainError.WebhookConfigNotFound("1"),
            DomainError.InvalidBarcode(),
            DomainError.InvalidStatusTransition("A", "B"),
            DomainError.CnabGenerationFailed(),
            DomainError.CnabProcessingFailed(),
            DomainError.FileStorageFailed(),
            DomainError.LayoutTemplateNotFound("template"),
            DomainError.WebhookNotFound("1")
        )

        // Assert
        errors.forEach { error ->
            assertTrue(error.message.isNotEmpty(), "Error ${error::class.simpleName} should have non-empty message")
        }
    }
}
