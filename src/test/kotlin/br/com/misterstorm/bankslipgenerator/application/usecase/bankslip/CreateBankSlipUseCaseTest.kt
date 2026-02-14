package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import arrow.core.Either
import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEvent
import br.com.misterstorm.bankslipgenerator.domain.event.DomainEventPublisher
import br.com.misterstorm.bankslipgenerator.domain.model.Address
import br.com.misterstorm.bankslipgenerator.domain.model.BankConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlip
import br.com.misterstorm.bankslipgenerator.domain.model.BankSlipStatus
import br.com.misterstorm.bankslipgenerator.domain.model.Beneficiary
import br.com.misterstorm.bankslipgenerator.domain.model.CnabConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.CnabVersion
import br.com.misterstorm.bankslipgenerator.domain.model.LayoutConfiguration
import br.com.misterstorm.bankslipgenerator.domain.model.Payer
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankSlipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.StructuredLogger
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertTrue

/**
 * Unit tests for CreateBankSlipUseCase following classist school with fixtures
 */
class CreateBankSlipUseCaseTest {

    private val logger = StructuredLogger("CreateBankSlipUseCaseTest")
    private val eventPublisher = DomainEventPublisherFixture()

    @Test
    fun `ensure creates bankSlip successfully when valid input is provided`() = runTest {
        // Arrange
        val bankSlipRepository = BankSlipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture()
        val useCase = CreateBankSlipUseCase(bankSlipRepository, bankConfigRepository, eventPublisher, logger)

        val input = CreateBankSlipUseCase.Input(
            bankCode = "001",
            amount = BigDecimal("100.00"),
            dueDate = LocalDate.now().plusDays(30),
            payer = createTestPayer(),
            beneficiary = createTestBeneficiary(),
            instructions = listOf("Não receber após vencimento")
        )

        // Act
        val result: Either<DomainError, BankSlip> = useCase(input)

        // Assert
        assertTrue(result.isRight())
        val bankSlip = result.getOrNull()!!
        assertTrue(bankSlip.bankCode == "001")
        assertTrue(bankSlip.amount == BigDecimal("100.00"))
        assertTrue(bankSlip.status == BankSlipStatus.CREATED)
    }

    @Test
    fun `ensure fails to create bankSlip when bank configuration not found`() = runTest {
        // Arrange
        val bankSlipRepository = BankSlipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture(shouldFail = true)
        val useCase = CreateBankSlipUseCase(bankSlipRepository, bankConfigRepository, eventPublisher, logger)

        val input = CreateBankSlipUseCase.Input(
            bankCode = "999",
            amount = BigDecimal("100.00"),
            dueDate = LocalDate.now().plusDays(30),
            payer = createTestPayer(),
            beneficiary = createTestBeneficiary()
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.BankConfigurationNotFound)
        }
    }

    @Test
    fun `ensure fails to create bankSlip when due date is in the past`() = runTest {
        // Arrange
        val bankSlipRepository = BankSlipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture()
        val useCase = CreateBankSlipUseCase(bankSlipRepository, bankConfigRepository, eventPublisher, logger)

        val input = CreateBankSlipUseCase.Input(
            bankCode = "001",
            amount = BigDecimal("100.00"),
            dueDate = LocalDate.now().minusDays(1),
            payer = createTestPayer(),
            beneficiary = createTestBeneficiary()
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.InvalidDueDate)
        }
    }

    @Test
    fun `ensure fails to create bankSlip when amount is zero or negative`() = runTest {
        // Arrange
        val bankSlipRepository = BankSlipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture()
        val useCase = CreateBankSlipUseCase(bankSlipRepository, bankConfigRepository, eventPublisher, logger)

        val input = CreateBankSlipUseCase.Input(
            bankCode = "001",
            amount = BigDecimal.ZERO,
            dueDate = LocalDate.now().plusDays(30),
            payer = createTestPayer(),
            beneficiary = createTestBeneficiary()
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isLeft())
        result.onLeft { error ->
            assertTrue(error is DomainError.InvalidAmount)
        }
    }

    // Test fixtures
    private fun createTestPayer(): Payer = Payer(
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
    )

    private fun createTestBeneficiary(): Beneficiary = Beneficiary(
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
    )
}

/**
 * Fixture (stub) for BankSlipRepository
 */
class BankSlipRepositoryFixture : BankSlipRepository {
    private val storage = mutableMapOf<UUID, BankSlip>()

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

/**
 * Fixture (stub) for BankConfigurationRepository
 */
class BankConfigurationRepositoryFixture(
    private val shouldFail: Boolean = false
) : BankConfigurationRepository {

    override suspend fun save(configuration: BankConfiguration) =
        Either.Right(configuration)

    override suspend fun findById(id: UUID) =
        Either.Left(DomainError.BankConfigurationNotFound(id.toString()))

    override suspend fun findByBankCode(bankCode: String) = if (shouldFail) {
        Either.Left(DomainError.BankConfigurationNotFound(bankCode))
    } else {
        Either.Right(createTestBankConfiguration(bankCode))
    }

    override suspend fun findAll(): Either<DomainError, List<BankConfiguration>> = Either.Right(emptyList())

    override suspend fun findAllActive(): Either<DomainError, List<BankConfiguration>> = Either.Right(emptyList())

    override suspend fun update(configuration: BankConfiguration) =
        Either.Right(configuration)

    override suspend fun delete(id: UUID) = Either.Right(Unit)

    private fun createTestBankConfiguration(bankCode: String) = BankConfiguration(
        id = UUID.randomUUID(),
        bankCode = bankCode,
        bankName = "Test Bank",
        layoutConfiguration = LayoutConfiguration(
            templateId = "default",
            logoUrl = null,
            customCss = null,
            fields = emptyMap()
        ),
        cnabConfiguration = CnabConfiguration(
            version = CnabVersion.CNAB240,
            headerLayout = "{}",
            detailLayout = "{}",
            trailerLayout = "{}"
        ),
        validationRules = emptyList(),
        isActive = true
    )
}

/**
 * Fixture (stub) for DomainEventPublisher
 */
class DomainEventPublisherFixture : DomainEventPublisher {
    val publishedEvents = mutableListOf<DomainEvent>()

    override suspend fun publish(event: DomainEvent) {
        publishedEvents.add(event)
    }

    override suspend fun publishAll(events: List<DomainEvent>) {
        publishedEvents.addAll(events)
    }
}
