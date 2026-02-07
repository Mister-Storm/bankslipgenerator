package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.*
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.StructuredLogger
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for PayBankslipUseCase
 */
class PayBankslipUseCaseTest {

    private val logger = StructuredLogger("PayBankslipUseCaseTest")

    @Test
    fun `ensure pays bankslip successfully when bankslip is registered`() = runTest {
        // Arrange
        val bankslip = createTestBankslip(status = BankslipStatus.REGISTERED)
        val repository = BankslipRepositoryFixture(bankslip)
        val useCase = PayBankslipUseCase(repository, logger)

        val input = PayBankslipUseCase.Input(
            bankslipId = bankslip.id,
            paymentDate = LocalDateTime.now(),
            paidAmount = bankslip.amount
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isRight())
        result.onRight { updatedBankslip ->
            assertEquals(BankslipStatus.PAID, updatedBankslip.status)
            assertEquals(input.paidAmount, updatedBankslip.paidAmount)
            assertTrue(updatedBankslip.paymentDate != null)
        }
    }

    @Test
    fun `ensure fails to pay bankslip when already paid`() = runTest {
        // Arrange
        val bankslip = createTestBankslip(
            status = BankslipStatus.PAID,
            paymentDate = LocalDateTime.now(),
            paidAmount = BigDecimal("100.00")
        )
        val repository = BankslipRepositoryFixture(bankslip)
        val useCase = PayBankslipUseCase(repository, logger)

        val input = PayBankslipUseCase.Input(
            bankslipId = bankslip.id,
            paymentDate = LocalDateTime.now(),
            paidAmount = bankslip.amount
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankslipAlreadyPaid)
        }
    }

    @Test
    fun `ensure fails to pay bankslip when cancelled`() = runTest {
        // Arrange
        val bankslip = createTestBankslip(status = BankslipStatus.CANCELLED)
        val repository = BankslipRepositoryFixture(bankslip)
        val useCase = PayBankslipUseCase(repository, logger)

        val input = PayBankslipUseCase.Input(
            bankslipId = bankslip.id,
            paymentDate = LocalDateTime.now(),
            paidAmount = bankslip.amount
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankslipAlreadyCancelled)
        }
    }

    @Test
    fun `ensure fails to pay bankslip when not found`() = runTest {
        // Arrange
        val repository = BankslipRepositoryFixture()
        val useCase = PayBankslipUseCase(repository, logger)

        val input = PayBankslipUseCase.Input(
            bankslipId = UUID.randomUUID(),
            paymentDate = LocalDateTime.now(),
            paidAmount = BigDecimal("100.00")
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankslipNotFound)
        }
    }

    private fun createTestBankslip(
        status: BankslipStatus = BankslipStatus.CREATED,
        paymentDate: LocalDateTime? = null,
        paidAmount: BigDecimal? = null
    ): Bankslip {
        val now = LocalDateTime.now()
        return Bankslip(
            id = UUID.randomUUID(),
            bankCode = "001",
            documentNumber = "1234567890",
            barcode = "00190000000000000001234567890",
            digitableLine = "00190.00000 00000.000001 23456.789001 0 00000000010000",
            amount = BigDecimal("100.00"),
            dueDate = LocalDate.now().plusDays(30),
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
            paymentDate = paymentDate,
            paidAmount = paidAmount,
            createdAt = now,
            updatedAt = now
        )
    }

    private class BankslipRepositoryFixture(
        private val existingBankslip: Bankslip? = null
    ) : br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository {
        private val storage = mutableMapOf<UUID, Bankslip>()

        init {
            existingBankslip?.let { storage[it.id] = it }
        }

        override suspend fun save(bankslip: Bankslip) = arrow.core.Either.Right(
            bankslip.also { storage[it.id] = it }
        )

        override suspend fun findById(id: UUID) = storage[id]
            ?.let { arrow.core.Either.Right(it) }
            ?: arrow.core.Either.Left(DomainError.BankslipNotFound(id.toString()))

        override suspend fun findByDocumentNumber(documentNumber: String) =
            arrow.core.Either.Left(DomainError.BankslipNotFound(documentNumber))

        override suspend fun findByBarcode(barcode: String) =
            arrow.core.Either.Left(DomainError.BankslipNotFound(barcode))

        override suspend fun findByStatus(status: BankslipStatus, page: Int, size: Int) =
            arrow.core.Either.Right(emptyList())

        override suspend fun findByDueDateBetween(startDate: LocalDate, endDate: LocalDate) =
            arrow.core.Either.Right(emptyList())

        override suspend fun findByPayerDocumentNumber(documentNumber: String, page: Int, size: Int) =
            arrow.core.Either.Right(emptyList())

        override suspend fun update(bankslip: Bankslip) =
            arrow.core.Either.Right(bankslip.also { storage[it.id] = it })

        override suspend fun delete(id: UUID) = arrow.core.Either.Right(Unit)

        override suspend fun softDelete(id: UUID) = arrow.core.Either.Right(Unit)
    }
}

