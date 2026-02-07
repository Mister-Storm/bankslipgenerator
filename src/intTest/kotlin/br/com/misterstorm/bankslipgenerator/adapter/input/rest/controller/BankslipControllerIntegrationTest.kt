package br.com.misterstorm.bankslipgenerator.adapter.input.rest.controller

import br.com.misterstorm.bankslipgenerator.adapter.input.rest.dto.*
import br.com.misterstorm.bankslipgenerator.domain.model.*
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Integration tests for BankslipController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BankslipControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun `ensure creates bankslip successfully via REST API`() {
        // Arrange
        val request = createValidBankslipRequest()

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
    fun `ensure retrieves bankslip by id via REST API`() {
        // Arrange - Create a bankslip first
        val request = createValidBankslipRequest()
        val createResponse = Given {
            contentType(ContentType.JSON)
            body(request)
            header("API-Version", "v1")
        } When {
            post("/api/bankslips")
        } Then {
            statusCode(201)
            extract().response()
        }

        val bankslipId = createResponse.path<String>("id")

        // Act & Assert
        Given {
            header("API-Version", "v1")
        } When {
            get("/api/bankslips/$bankslipId")
        } Then {
            statusCode(200)
            body("id", equalTo(bankslipId))
            body("status", equalTo("CREATED"))
        }
    }

    @Test
    fun `ensure fails to create bankslip with invalid due date`() {
        // Arrange
        val request = createValidBankslipRequest().copy(
            dueDate = LocalDate.now().minusDays(1)
        )

        // Act & Assert
        Given {
            contentType(ContentType.JSON)
            body(request)
            header("API-Version", "v1")
        } When {
            post("/api/bankslips")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `ensure soft deletes bankslip via REST API`() {
        // Arrange - Create a bankslip first
        val request = createValidBankslipRequest()
        val createResponse = Given {
            contentType(ContentType.JSON)
            body(request)
            header("API-Version", "v1")
        } When {
            post("/api/bankslips")
        } Then {
            statusCode(201)
            extract().response()
        }

        val bankslipId = createResponse.path<String>("id")

        // Act & Assert
        Given {
            header("API-Version", "v1")
        } When {
            delete("/api/bankslips/$bankslipId")
        } Then {
            statusCode(204)
        }
    }

    private fun createValidBankslipRequest() = CreateBankslipRequest(
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
        @Container
        private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("bankslipgenerator_test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }
}

