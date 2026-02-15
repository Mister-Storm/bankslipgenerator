package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.AddressDto
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.BeneficiaryDto
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.CreateBankSlipRequest
import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.PayerDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Integration tests for BankSlipController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BankSlipControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun `ensure creates bankSlip successfully via REST API`() {
        // Arrange
        val request = createValidRequest()

        // Act & Assert
        Given {
            contentType(ContentType.JSON)
            body(request)
            header("API-Version", "v1")
        } When {
            post("/api/bankslips")
        } Then {
            statusCode(201)
            body("bankCode", equalTo("001"))
            body("amount", equalTo(100.00f))
            body("status", equalTo("CREATED"))
            body("id", notNullValue())
            body("barcode", notNullValue())
            body("digitableLine", notNullValue())
        }
    }

    @Test
    fun `ensure gets bankSlip by id`() {
        // Arrange - Create bankSlip first
        val request = createValidRequest()

        val bankSlipId: String = Given {
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/api/bankslips")
        } Extract {
            path("id")
        }

        // Act & Assert
        Given {
            header("API-Version", "v1")
        } When {
            get("/api/bankslips/$bankSlipId")
        } Then {
            statusCode(200)
            body("id", equalTo(bankSlipId))
            body("bankCode", equalTo("001"))
        }
    }

    @Test
    fun `ensure returns 404 for non-existent bankSlip`() {
        Given {
            header("API-Version", "v1")
        } When {
            get("/api/bankslips/00000000-0000-0000-0000-000000000000")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `ensure soft deletes bankSlip via REST API`() {
        // Arrange - Create a bankSlip first
        val request = createValidRequest()

        val bankSlipId: String = Given {
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/api/bankslips")
        } Extract {
            path("id")
        }

        // Act & Assert
        Given {
            header("API-Version", "v1")
        } When {
            delete("/api/bankslips/$bankSlipId")
        } Then {
            statusCode(204)
        }
    }

    private fun createValidRequest() = CreateBankSlipRequest(
        bankCode = "001",
        amount = BigDecimal("100.00"),
        dueDate = LocalDate.now().plusDays(30),
        payer = PayerDto(
            name = "John Doe",
            documentNumber = "12345678900",
            address = AddressDto(
                street = "Main Street",
                number = "123",
                neighborhood = "Downtown",
                city = "São Paulo",
                state = "SP",
                zipCode = "01234567"
            )
        ),
        beneficiary = BeneficiaryDto(
            name = "Company Inc",
            documentNumber = "12345678000190",
            address = AddressDto(
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
        instructions = listOf("Não receber após vencimento")
    )

    companion object {
        lateinit var postgresContainer: PostgreSQLContainer<*>
        @BeforeAll
        @JvmStatic
        fun startContainer() {
            postgresContainer = PostgreSQLContainer("postgres:16").apply {
                withDatabaseName("bankslipgenerator_test")
                withUsername("test")
                withPassword("test")
                start()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.flyway.enabled") { "true" }
        }
    }
}

class ContainerSmokeTest {

    @Test
    fun testContainer() {
        PostgreSQLContainer("postgres:16").start()
    }
}

