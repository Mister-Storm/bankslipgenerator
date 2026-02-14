package br.com.misterstorm.bankslipgenerator.domain.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for BankSlip domain model
 */
class BankSlipTest {

    @Test
    fun `ensure isDeleted returns true when deletedAt is set`() {
        // Arrange
        val bankSlip = createBankSlip(deletedAt = LocalDateTime.now())

        // Act & Assert
        assertTrue(bankSlip.isDeleted())
    }

    @Test
    fun `ensure isDeleted returns false when deletedAt is null`() {
        // Arrange
        val bankSlip = createBankSlip(deletedAt = null)

        // Act & Assert
        assertFalse(bankSlip.isDeleted())
    }

    @Test
    fun `ensure canTransitionTo allows CREATED to REGISTERED`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.CREATED)

        // Act & Assert
        assertTrue(bankSlip.canTransitionTo(BankSlipStatus.REGISTERED))
    }

    @Test
    fun `ensure canTransitionTo allows CREATED to CANCELLED`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.CREATED)

        // Act & Assert
        assertTrue(bankSlip.canTransitionTo(BankSlipStatus.CANCELLED))
    }

    @Test
    fun `ensure canTransitionTo allows CREATED to EXPIRED`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.CREATED)

        // Act & Assert
        assertTrue(bankSlip.canTransitionTo(BankSlipStatus.EXPIRED))
    }

    @Test
    fun `ensure canTransitionTo does not allow CREATED to PAID directly`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.CREATED)

        // Act & Assert
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.PAID))
    }

    @Test
    fun `ensure canTransitionTo allows REGISTERED to PAID`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.REGISTERED)

        // Act & Assert
        assertTrue(bankSlip.canTransitionTo(BankSlipStatus.PAID))
    }

    @Test
    fun `ensure canTransitionTo allows REGISTERED to CANCELLED`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.REGISTERED)

        // Act & Assert
        assertTrue(bankSlip.canTransitionTo(BankSlipStatus.CANCELLED))
    }

    @Test
    fun `ensure canTransitionTo does not allow PAID to any status`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.PAID)

        // Act & Assert
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.CREATED))
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.REGISTERED))
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.CANCELLED))
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.EXPIRED))
    }

    @Test
    fun `ensure canTransitionTo does not allow CANCELLED to any status`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.CANCELLED)

        // Act & Assert
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.CREATED))
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.REGISTERED))
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.PAID))
        assertFalse(bankSlip.canTransitionTo(BankSlipStatus.EXPIRED))
    }

    @Test
    fun `ensure canTransitionTo allows EXPIRED to PAID`() {
        // Arrange
        val bankSlip = createBankSlip(status = BankSlipStatus.EXPIRED)

        // Act & Assert
        assertTrue(bankSlip.canTransitionTo(BankSlipStatus.PAID))
    }

    @Test
    fun `ensure isExpired returns true when dueDate is past and status is not PAID`() {
        // Arrange
        val bankSlip = createBankSlip(
            status = BankSlipStatus.REGISTERED,
            dueDate = LocalDate.now().minusDays(1)
        )

        // Act & Assert
        assertTrue(bankSlip.isExpired())
    }

    @Test
    fun `ensure isExpired returns false when dueDate is in future`() {
        // Arrange
        val bankSlip = createBankSlip(
            status = BankSlipStatus.REGISTERED,
            dueDate = LocalDate.now().plusDays(30)
        )

        // Act & Assert
        assertFalse(bankSlip.isExpired())
    }

    @Test
    fun `ensure isExpired returns false when status is PAID even if dueDate is past`() {
        // Arrange
        val bankSlip = createBankSlip(
            status = BankSlipStatus.PAID,
            dueDate = LocalDate.now().minusDays(1)
        )

        // Act & Assert
        assertFalse(bankSlip.isExpired())
    }

    @Test
    fun `ensure BankSlipStatus has all expected values`() {
        // Act
        val statuses = BankSlipStatus.entries

        // Assert
        assertEquals(5, statuses.size)
        assertTrue(statuses.contains(BankSlipStatus.CREATED))
        assertTrue(statuses.contains(BankSlipStatus.REGISTERED))
        assertTrue(statuses.contains(BankSlipStatus.PAID))
        assertTrue(statuses.contains(BankSlipStatus.CANCELLED))
        assertTrue(statuses.contains(BankSlipStatus.EXPIRED))
    }

    private fun createBankSlip(
        status: BankSlipStatus = BankSlipStatus.CREATED,
        dueDate: LocalDate = LocalDate.now().plusDays(30),
        deletedAt: LocalDateTime? = null
    ): BankSlip {
        val now = LocalDateTime.now()
        return BankSlip(
            id = UUID.randomUUID(),
            bankCode = "001",
            documentNumber = "1234567890",
            barcode = "00190000000000000001234567890",
            digitableLine = "00190.00000 00000.000001 23456.789001 0 00000000010000",
            amount = BigDecimal("100.00"),
            dueDate = dueDate,
            issueDate = LocalDate.now(),
            status = status,
            payer = Payer(
                name = "John Doe",
                documentNumber = "12345678900",
                address = Address(
                    street = "Main Street",
                    number = "123",
                    neighborhood = "Downtown",
                    city = "São Paulo",
                    state = "SP",
                    zipCode = "01234567"
                )
            ),
            beneficiary = Beneficiary(
                name = "Company Inc",
                documentNumber = "12345678000190",
                address = Address(
                    street = "Corporate Avenue",
                    number = "456",
                    neighborhood = "Business District",
                    city = "São Paulo",
                    state = "SP",
                    zipCode = "01234567"
                ),
                agencyNumber = "1234",
                accountNumber = "567890",
                accountDigit = "1"
            ),
            instructions = emptyList(),
            createdAt = now,
            updatedAt = now,
            deletedAt = deletedAt
        )
    }
}

