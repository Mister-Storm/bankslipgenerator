package br.com.misterstorm.bankslipgenerator.application.usecase.bankslip

import br.com.misterstorm.bankslipgenerator.domain.error.DomainError
import br.com.misterstorm.bankslipgenerator.domain.model.*
import br.com.misterstorm.bankslipgenerator.domain.port.BankConfigurationRepository
import br.com.misterstorm.bankslipgenerator.domain.port.BankslipRepository
import br.com.misterstorm.bankslipgenerator.infrastructure.logging.StructuredLogger
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertTrue

/**
 * Unit tests for CreateBankslipUseCase following classist school with fixtures
 */
class CreateBankslipUseCaseTest {

    private val logger = StructuredLogger("CreateBankslipUseCaseTest")

    @Test
    fun `ensure creates bankslip successfully when valid input is provided`() = runTest {
        // Arrange
        val bankslipRepository = BankslipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture()
        val useCase = CreateBankslipUseCase(bankslipRepository, bankConfigRepository, logger)

        val input = CreateBankslipUseCase.Input(
            bankCode = "001",
            amount = BigDecimal("100.00"),
            dueDate = LocalDate.now().plusDays(30),
            payer = createTestPayer(),
            beneficiary = createTestBeneficiary(),
            instructions = listOf("Não receber após vencimento")
        )

        // Act
        val result = useCase(input)

        // Assert
        assertTrue(result.isRight())
        result.onRight { bankslip ->
            assertTrue(bankslip.bankCode == "001")
            assertTrue(bankslip.amount == BigDecimal("100.00"))
            assertTrue(bankslip.status == BankslipStatus.CREATED)
        }
    }

    @Test
    fun `ensure fails to create bankslip when bank configuration not found`() = runTest {
        // Arrange
        val bankslipRepository = BankslipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture(shouldFail = true)
        val useCase = CreateBankslipUseCase(bankslipRepository, bankConfigRepository, logger)

        val input = CreateBankslipUseCase.Input(
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
    fun `ensure fails to create bankslip when due date is in the past`() = runTest {
        // Arrange
        val bankslipRepository = BankslipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture()
        val useCase = CreateBankslipUseCase(bankslipRepository, bankConfigRepository, logger)

        val input = CreateBankslipUseCase.Input(
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
    fun `ensure fails to create bankslip when amount is zero or negative`() = runTest {
        // Arrange
        val bankslipRepository = BankslipRepositoryFixture()
        val bankConfigRepository = BankConfigurationRepositoryFixture()
        val useCase = CreateBankslipUseCase(bankslipRepository, bankConfigRepository, logger)

        val input = CreateBankslipUseCase.Input(
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
 * Fixture (stub) for BankslipRepository
 */
class BankslipRepositoryFixture : BankslipRepository {
    private val storage = mutableMapOf<UUID, Bankslip>()

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

/**
 * Fixture (stub) for BankConfigurationRepository
 */
class BankConfigurationRepositoryFixture(
    private val shouldFail: Boolean = false
) : BankConfigurationRepository {

    override suspend fun save(configuration: BankConfiguration) =
        arrow.core.Either.Right(configuration)

    override suspend fun findById(id: UUID) =
        arrow.core.Either.Left(DomainError.BankConfigurationNotFound(id.toString()))

    override suspend fun findByBankCode(bankCode: String) = if (shouldFail) {
        arrow.core.Either.Left(DomainError.BankConfigurationNotFound(bankCode))
    } else {
        arrow.core.Either.Right(createTestBankConfiguration(bankCode))
    }

    override suspend fun findAll() = arrow.core.Either.Right(emptyList())

    override suspend fun findAllActive() = arrow.core.Either.Right(emptyList())

    override suspend fun update(configuration: BankConfiguration) =
        arrow.core.Either.Right(configuration)

    override suspend fun delete(id: UUID) = arrow.core.Either.Right(Unit)

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

