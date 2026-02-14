package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.Address
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.model.Beneficiary
import br.com.misterstorm.bankslipgenerator.domain.model.Payer
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.StructuredLogger
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for PayBankSlipUseCase
 */
class PayBankSlipUseCaseTest {

    private val logger = StructuredLogger("PayBankSlipUseCaseTest")
    private val eventPublisher = TestDomainEventPublisher()

    @Test
    fun `ensure pays bankSlip successfully when bankSlip is registered`() = runTest {
        // Arrange
        val bankSlip = createTestBankSlip(status = BankSlipStatus.REGISTERED)
        val repository = PayBankSlipRepositoryFixture(bankSlip)
        val useCase = PayBankSlipUseCase(repository, eventPublisher, logger)

        val input = PayBankSlipUseCase.Input(
            bankSlipId = bankSlip.id,
            paymentDate = LocalDateTime.now(),
            paidAmount = bankSlip.amount
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isRight())
        val updatedBankSlip = result.getOrNull()!!
        assertEquals(BankSlipStatus.PAID, updatedBankSlip.status)
        assertEquals(input.paidAmount, updatedBankSlip.paidAmount)
        assertTrue(updatedBankSlip.paymentDate != null)
    }

    @Test
    fun `ensure fails to pay bankSlip when already paid`() = runTest {
        // Arrange
        val bankSlip = createTestBankSlip(
            status = BankSlipStatus.PAID,
            paymentDate = LocalDateTime.now(),
            paidAmount = BigDecimal("100.00")
        )
        val repository = PayBankSlipRepositoryFixture(bankSlip)
        val useCase = PayBankSlipUseCase(repository, eventPublisher, logger)

        val input = PayBankSlipUseCase.Input(
            bankSlipId = bankSlip.id,
            paymentDate = LocalDateTime.now(),
            paidAmount = bankSlip.amount
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankSlipAlreadyPaid)
        }
    }

    @Test
    fun `ensure fails to pay bankSlip when cancelled`() = runTest {
        // Arrange
        val bankSlip = createTestBankSlip(status = BankSlipStatus.CANCELLED)
        val repository = PayBankSlipRepositoryFixture(bankSlip)
        val useCase = PayBankSlipUseCase(repository, eventPublisher, logger)

        val input = PayBankSlipUseCase.Input(
            bankSlipId = bankSlip.id,
            paymentDate = LocalDateTime.now(),
            paidAmount = bankSlip.amount
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankSlipAlreadyCancelled)
        }
    }

    @Test
    fun `ensure fails to pay bankSlip when not found`() = runTest {
        // Arrange
        val repository = PayBankSlipRepositoryFixture()
        val useCase = PayBankSlipUseCase(repository, eventPublisher, logger)

        val input = PayBankSlipUseCase.Input(
            bankSlipId = UUID.randomUUID(),
            paymentDate = LocalDateTime.now(),
            paidAmount = BigDecimal("100.00")
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankSlipNotFound)
        }
    }

    private fun createTestBankSlip(
        status: BankSlipStatus = BankSlipStatus.CREATED,
        paymentDate: LocalDateTime? = null,
        paidAmount: BigDecimal? = null
    ): BankSlip {
        val now = LocalDateTime.now()
        return BankSlip(
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

    private class PayBankSlipRepositoryFixture(
        existingBankSlip: BankSlip? = null
    ) : BankSlipRepository {
        private val storage = mutableMapOf<UUID, BankSlip>()

        init {
            existingBankSlip?.let { storage[it.id] = it }
        }

        override suspend fun save(bankSlip: BankSlip) = Either.Right(
            bankSlip.also { storage[it.id] = it }
        )

        override suspend fun findById(id: UUID) = storage[id]
            ?.let { Either.Right(it) }
            ?: Either.Left(DomainError.BankSlipNotFound(id.toString()))

        override suspend fun findByDocumentNumber(documentNumber: String) =
            Either.Left(DomainError.BankSlipNotFound(documentNumber))

        override suspend fun findByBarcode(barcode: String) =
            Either.Left(DomainError.BankSlipNotFound(barcode))

        override suspend fun findByStatus(status: BankSlipStatus, page: Int, size: Int): Either<DomainError, List<BankSlip>> =
            Either.Right(emptyList())

        override suspend fun findByDueDateBetween(startDate: LocalDate, endDate: LocalDate): Either<DomainError, List<BankSlip>> =
            Either.Right(emptyList())

        override suspend fun findByPayerDocumentNumber(documentNumber: String, page: Int, size: Int): Either<DomainError, List<BankSlip>> =
            Either.Right(emptyList())

        override suspend fun update(bankSlip: BankSlip) =
            Either.Right(bankSlip.also { storage[it.id] = it })

        override suspend fun delete(id: UUID) = Either.Right(Unit)

        override suspend fun softDelete(id: UUID) = Either.Right(Unit)
    }
}

/**
 * Fixture (stub) for DomainEventPublisher
 */
class TestDomainEventPublisher : DomainEventPublisher {
    val publishedEvents = mutableListOf<DomainEvent>()

    override suspend fun publish(event: DomainEvent) {
        publishedEvents.add(event)
    }

    override suspend fun publishAll(events: List<DomainEvent>) {
        publishedEvents.addAll(events)
    }
}
